package com.codeclash.runnerservice.testcase.adapter.config;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.event.LoggingDeadLetterRecoverer;
import com.codeclash.common.exception.CodedException;
import com.codeclash.contracts.problem.event.TestCaseCdcEventData;
import com.codeclash.runnerservice.testcase.model.exception.QuestionNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import tools.jackson.core.type.TypeReference;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TestCaseCdcConsumerProperties.class)
class TestCaseConfig {

  private static final String DLT_SUFFIX = ".dlt.runner";
  private static final int DLT_PARTITIONS = 1;
  private static final int DLT_PARTITION = 0;
  private static final String AUTO_OFFSET_RESET = "earliest";

  @SuppressWarnings("unchecked")
  private static ConsumerFactory<Object, Object> consumerFactory(
      KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
    Map<String, Object> config = kafkaProperties.buildConsumerProperties();
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, TestCaseConfig.AUTO_OFFSET_RESET);
    JacksonJsonDeserializer<CdcEvent<TestCaseCdcEventData>> valueDeserializer =
        new JacksonJsonDeserializer<>(new TypeReference<CdcEvent<TestCaseCdcEventData>>() {})
            .ignoreTypeHeaders();
    ConsumerFactory<?, ?> consumerFactory =
        new DefaultKafkaConsumerFactory<>(
            config, new StringDeserializer(), new ErrorHandlingDeserializer<>(valueDeserializer));
    ConsumerFactory<Object, Object> objectFactory =
        (ConsumerFactory<Object, Object>) consumerFactory;
    objectFactory.addListener(new MicrometerConsumerListener<>(meterRegistry));
    return objectFactory;
  }

  private static DefaultErrorHandler errorHandler(
      TestCaseCdcConsumerProperties properties,
      KafkaProperties kafkaProperties,
      KafkaOperations<Object, Object> kafkaOperations,
      ObservationRegistry observationRegistry,
      MeterRegistry meterRegistry) {
    ExponentialBackOffWithMaxRetries backOff =
        new ExponentialBackOffWithMaxRetries(properties.retry().maxAttempts() - 1);
    backOff.setInitialInterval(properties.retry().initialInterval().toMillis());
    backOff.setMultiplier(properties.retry().multiplier());
    backOff.setMaxInterval(properties.retry().maxInterval().toMillis());

    Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
    templates.put(
        byte[].class,
        TestCaseConfig.bytesTemplate(kafkaProperties, observationRegistry, meterRegistry));
    templates.put(Object.class, kafkaOperations);
    String deadLetterTopic = TestCaseConfig.deadLetterTopic(properties);
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(
            templates, (_, _) -> new TopicPartition(deadLetterTopic, TestCaseConfig.DLT_PARTITION));

    DefaultErrorHandler errorHandler =
        new DefaultErrorHandler(new LoggingDeadLetterRecoverer(recoverer), backOff);
    errorHandler.addNotRetryableExceptions(CodedException.class);
    errorHandler.addRetryableExceptions(QuestionNotFoundException.class);
    return errorHandler;
  }

  private static KafkaTemplate<String, byte[]> bytesTemplate(
      KafkaProperties kafkaProperties,
      ObservationRegistry observationRegistry,
      MeterRegistry meterRegistry) {
    DefaultKafkaProducerFactory<String, byte[]> producerFactory =
        new DefaultKafkaProducerFactory<>(
            kafkaProperties.buildProducerProperties(),
            new StringSerializer(),
            new ByteArraySerializer());
    producerFactory.addListener(new MicrometerProducerListener<>(meterRegistry));
    KafkaTemplate<String, byte[]> template = new KafkaTemplate<>(producerFactory);
    template.setObservationEnabled(true);
    template.setObservationRegistry(observationRegistry);
    return template;
  }

  private static String deadLetterTopic(TestCaseCdcConsumerProperties properties) {
    return properties.topic() + TestCaseConfig.DLT_SUFFIX;
  }

  @Bean
  NewTopic testCaseCdcDeadLetterTopic(TestCaseCdcConsumerProperties properties) {
    return TopicBuilder.name(TestCaseConfig.deadLetterTopic(properties))
        .partitions(TestCaseConfig.DLT_PARTITIONS)
        .config(
            TopicConfig.MAX_MESSAGE_BYTES_CONFIG,
            String.valueOf(properties.deadLetterMaxMessageSize().toBytes()))
        .build();
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<Object, Object> testCaseCdcListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      TestCaseCdcConsumerProperties properties,
      KafkaProperties kafkaProperties,
      KafkaOperations<Object, Object> kafkaOperations,
      ObservationRegistry observationRegistry,
      MeterRegistry meterRegistry) {
    ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    configurer.configure(factory, TestCaseConfig.consumerFactory(kafkaProperties, meterRegistry));
    factory.setCommonErrorHandler(
        TestCaseConfig.errorHandler(
            properties, kafkaProperties, kafkaOperations, observationRegistry, meterRegistry));
    return factory;
  }
}

package com.codeclash.contestservice.question.adapter.config;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.event.LoggingDeadLetterRecoverer;
import com.codeclash.common.exception.CodedException;
import com.codeclash.contracts.problem.event.QuestionCdcEventData;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
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
@EnableConfigurationProperties(QuestionCdcConsumerProperties.class)
class QuestionConfig {

  private static final String DLT_SUFFIX = ".dlt.contest";
  private static final int DLT_PARTITIONS = 1;
  private static final int DLT_PARTITION = 0;
  private static final String AUTO_OFFSET_RESET = "earliest";

  @SuppressWarnings("unchecked")
  private static ConsumerFactory<Object, Object> consumerFactory(
      KafkaProperties kafkaProperties, MeterRegistry meterRegistry) {
    Map<String, Object> config = kafkaProperties.buildConsumerProperties();
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, QuestionConfig.AUTO_OFFSET_RESET);
    JacksonJsonDeserializer<CdcEvent<QuestionCdcEventData>> valueDeserializer =
        new JacksonJsonDeserializer<>(new TypeReference<CdcEvent<QuestionCdcEventData>>() {})
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
      QuestionCdcConsumerProperties properties,
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
        QuestionConfig.bytesTemplate(kafkaProperties, observationRegistry, meterRegistry));
    templates.put(Object.class, kafkaOperations);
    String deadLetterTopic = QuestionConfig.deadLetterTopic(properties);
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(
            templates, (_, _) -> new TopicPartition(deadLetterTopic, QuestionConfig.DLT_PARTITION));

    DefaultErrorHandler errorHandler =
        new DefaultErrorHandler(new LoggingDeadLetterRecoverer(recoverer), backOff);
    errorHandler.addNotRetryableExceptions(CodedException.class);
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

  private static String deadLetterTopic(QuestionCdcConsumerProperties properties) {
    return properties.topic() + QuestionConfig.DLT_SUFFIX;
  }

  @Bean
  NewTopic questionCdcDeadLetterTopic(QuestionCdcConsumerProperties properties) {
    return TopicBuilder.name(QuestionConfig.deadLetterTopic(properties))
        .partitions(QuestionConfig.DLT_PARTITIONS)
        .build();
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<Object, Object> questionCdcListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      QuestionCdcConsumerProperties properties,
      KafkaProperties kafkaProperties,
      KafkaOperations<Object, Object> kafkaOperations,
      ObservationRegistry observationRegistry,
      MeterRegistry meterRegistry) {
    ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    configurer.configure(factory, QuestionConfig.consumerFactory(kafkaProperties, meterRegistry));
    factory.setCommonErrorHandler(
        QuestionConfig.errorHandler(
            properties, kafkaProperties, kafkaOperations, observationRegistry, meterRegistry));
    return factory;
  }
}

package com.codeclash.common.event;

import io.namastack.outbox.kafka.KafkaOutboxRouting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OutboxKafkaRoutingConfig {

  @Bean
  KafkaOutboxRouting kafkaOutboxRouting() {
    return OutboxKafkaRouting.create();
  }
}

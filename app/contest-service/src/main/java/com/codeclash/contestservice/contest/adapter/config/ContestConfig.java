package com.codeclash.contestservice.contest.adapter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(ContestProperties.class)
class ContestConfig {

  @Bean
  NewTopic contestCdcTopic(ContestProperties properties) {
    return TopicBuilder.name(properties.cdcTopic().name())
        .partitions(properties.cdcTopic().partitions())
        .build();
  }
}

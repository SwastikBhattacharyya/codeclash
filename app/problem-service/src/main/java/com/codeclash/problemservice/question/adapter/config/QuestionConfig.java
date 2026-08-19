package com.codeclash.problemservice.question.adapter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(QuestionProperties.class)
class QuestionConfig {

  @Bean
  NewTopic questionCdcTopic(QuestionProperties properties) {
    return TopicBuilder.name(properties.cdcTopic().name())
        .partitions(properties.cdcTopic().partitions())
        .build();
  }
}

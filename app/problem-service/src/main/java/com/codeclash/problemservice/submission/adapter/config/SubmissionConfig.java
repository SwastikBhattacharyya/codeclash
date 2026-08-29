package com.codeclash.problemservice.submission.adapter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SubmissionProperties.class)
class SubmissionConfig {

  @Bean
  NewTopic submissionCdcTopic(SubmissionProperties properties) {
    return TopicBuilder.name(properties.cdcTopic().name())
        .partitions(properties.cdcTopic().partitions())
        .build();
  }
}

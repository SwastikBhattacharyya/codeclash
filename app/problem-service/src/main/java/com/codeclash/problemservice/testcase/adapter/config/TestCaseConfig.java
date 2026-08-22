package com.codeclash.problemservice.testcase.adapter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TestCaseProperties.class)
class TestCaseConfig {

  @Bean
  NewTopic testCaseCdcTopic(TestCaseProperties properties) {
    return TopicBuilder.name(properties.cdcTopic().name())
        .partitions(properties.cdcTopic().partitions())
        .config(
            TopicConfig.MAX_MESSAGE_BYTES_CONFIG,
            String.valueOf(properties.cdcTopicMaxMessageSize().toBytes()))
        .build();
  }
}

package com.codeclash.runnerservice.outbox.adapter.config;

import com.codeclash.common.event.EnableOutboxKafkaRouting;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableOutboxKafkaRouting
class OutboxConfig {}

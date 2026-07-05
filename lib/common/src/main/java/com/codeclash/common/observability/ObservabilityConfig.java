package com.codeclash.common.observability;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class ObservabilityConfig {

  @Bean
  ObservationPredicate parentlessObservationPredicate() {
    return new ParentlessObservationPredicate();
  }
}

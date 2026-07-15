package com.codeclash.authservice.user.adapter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Argon2Properties.class)
class PasswordEncoderConfig {

  @Bean
  PasswordEncoder passwordEncoder(Argon2Properties properties) {
    return new Argon2PasswordEncoder(
        properties.saltLength(),
        properties.hashLength(),
        properties.parallelism(),
        properties.memoryKib(),
        properties.iterations());
  }
}

package com.codeclash.problemservice.auth.adapter.config;

import com.codeclash.contracts.auth.session.EnableSessionAuthentication;
import com.codeclash.contracts.auth.session.SessionResolver;
import com.codeclash.contracts.auth.session.remote.AuthClientProperties;
import com.codeclash.contracts.auth.session.remote.RemoteSessionResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuthClientProperties.class)
@EnableSessionAuthentication
class AuthConfig {

  @Bean
  SessionResolver sessionResolver(RestClient.Builder builder, AuthClientProperties properties) {
    return RemoteSessionResolver.create(builder, properties);
  }
}

package com.codeclash.gatewayservice.session.adapter.config;

import com.codeclash.contracts.auth.session.SessionResolver;
import com.codeclash.contracts.auth.session.remote.AuthClientProperties;
import com.codeclash.contracts.auth.session.remote.RemoteSessionResolver;
import com.codeclash.contracts.auth.session.rest.AuthExceptionHandler;
import com.codeclash.gatewayservice.session.adapter.inbound.rest.SessionCheckFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuthClientProperties.class)
@Import(AuthExceptionHandler.class)
class SessionCheckConfig {

  private static final String HANDLER_EXCEPTION_RESOLVER = "handlerExceptionResolver";

  @Bean
  SessionResolver sessionResolver(RestClient.Builder builder, AuthClientProperties properties) {
    return RemoteSessionResolver.create(builder, properties);
  }

  @Bean
  SessionCheckFilter sessionCheckFilter(
      SessionResolver sessionResolver,
      @Qualifier(SessionCheckConfig.HANDLER_EXCEPTION_RESOLVER)
          HandlerExceptionResolver handlerExceptionResolver) {
    return new SessionCheckFilter(sessionResolver, handlerExceptionResolver);
  }
}

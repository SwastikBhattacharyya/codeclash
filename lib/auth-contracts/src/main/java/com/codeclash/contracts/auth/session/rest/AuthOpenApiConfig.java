package com.codeclash.contracts.auth.session.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.exception.AuthUnavailableException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.exception.UnauthenticatedException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AuthOpenApiConfig {

  @Bean
  OpenApiCustomizer authOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    List<String> permissions = List.of(AuthPermissions.SESSION_READ);
    return openApi -> {
      ApiSchemas.register(
          springDocProperties, openApi.getComponents(), RequiredPermissionsProperties.class);
      openApi
          .getComponents()
          .addExamples(
              AuthApiExamples.UNAUTHENTICATED, ApiExamples.error(new UnauthenticatedException()))
          .addExamples(
              AuthApiExamples.FORBIDDEN,
              ApiExamples.error(
                  new ForbiddenException(permissions),
                  new RequiredPermissionsProperties(permissions)))
          .addExamples(
              AuthApiExamples.AUTH_UNAVAILABLE,
              ApiExamples.error(new AuthUnavailableException()));
    };
  }
}

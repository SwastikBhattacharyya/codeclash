package com.codeclash.authservice.session.adapter.inbound.rest;

import com.codeclash.authservice.session.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.session.adapter.inbound.rest.dto.SessionIdProperties;
import com.codeclash.authservice.session.adapter.inbound.rest.dto.UserIdProperties;
import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.authservice.session.model.exception.InvalidCredentialsException;
import com.codeclash.authservice.session.model.exception.InvalidSessionIdException;
import com.codeclash.authservice.session.model.exception.InvalidUserIdException;
import com.codeclash.authservice.session.model.exception.AmbiguousCredentialsException;
import com.codeclash.authservice.session.model.exception.NullCredentialsException;
import com.codeclash.authservice.session.model.exception.NullSessionIdException;
import com.codeclash.authservice.session.model.exception.NullUserIdException;
import com.codeclash.authservice.session.model.exception.SessionNotFoundException;
import com.codeclash.authservice.session.model.exception.UnverifiedUserException;
import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import io.swagger.v3.oas.models.examples.Example;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SessionApiExamples {

  public static final String SESSION_NOT_FOUND = "session.SESSION_NOT_FOUND";
  public static final String INVALID_SESSION_ID = "session.INVALID_SESSION_ID";
  public static final String NULL_SESSION_ID = "session.NULL_SESSION_ID";
  public static final String INVALID_USER_ID = "session.INVALID_USER_ID";
  public static final String NULL_USER_ID = "session.NULL_USER_ID";
  public static final String INVALID_CREDENTIALS = "session.INVALID_CREDENTIALS";
  public static final String UNVERIFIED_USER = "session.UNVERIFIED_USER";
  public static final String NULL_CREDENTIALS = "session.NULL_CREDENTIALS";
  public static final String AMBIGUOUS_CREDENTIALS = "session.AMBIGUOUS_CREDENTIALS";
  public static final String FORBIDDEN_READ = "session.FORBIDDEN_READ";
  public static final String FORBIDDEN_READ_OWN = "session.FORBIDDEN_READ_OWN";
  public static final String FORBIDDEN_READ_EITHER = "session.FORBIDDEN_READ_EITHER";
  public static final String FORBIDDEN_WRITE = "session.FORBIDDEN_WRITE";
  public static final String FORBIDDEN_WRITE_OWN = "session.FORBIDDEN_WRITE_OWN";
  public static final String FORBIDDEN_WRITE_EITHER = "session.FORBIDDEN_WRITE_EITHER";
  public static final String UNKNOWN_SORT_FIELD = "session.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final List<String> SORT_FIELDS =
      List.of("created_at", "id", "last_used_at", "updated_at");

  private static Example forbidden(String... permissions) {
    List<String> missing = List.of(permissions);
    return ApiExamples.error(
        new ForbiddenException(missing), new RequiredPermissionsProperties(missing));
  }

  @Bean
  OpenApiCustomizer sessionOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          SessionIdProperties.class,
          UserIdProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              SessionApiExamples.SESSION_NOT_FOUND,
              ApiExamples.error(
                  new SessionNotFoundException(SessionApiExamples.SAMPLE_ID),
                  new SessionIdProperties(SessionApiExamples.SAMPLE_ID)))
          .addExamples(
              SessionApiExamples.INVALID_SESSION_ID,
              ApiExamples.error(
                  new InvalidSessionIdException(SessionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(
              SessionApiExamples.NULL_SESSION_ID,
              ApiExamples.error(new NullSessionIdException()))
          .addExamples(
              SessionApiExamples.INVALID_USER_ID,
              ApiExamples.error(
                  new InvalidUserIdException(SessionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(UserId.VERSION)))
          .addExamples(
              SessionApiExamples.NULL_USER_ID,
              ApiExamples.error(new NullUserIdException()))
          .addExamples(
              SessionApiExamples.INVALID_CREDENTIALS,
              ApiExamples.error(new InvalidCredentialsException()))
          .addExamples(
              SessionApiExamples.UNVERIFIED_USER,
              ApiExamples.error(new UnverifiedUserException()))
          .addExamples(
              SessionApiExamples.NULL_CREDENTIALS,
              ApiExamples.error(new NullCredentialsException()))
          .addExamples(
              SessionApiExamples.AMBIGUOUS_CREDENTIALS,
              ApiExamples.error(new AmbiguousCredentialsException()))
          .addExamples(
              SessionApiExamples.FORBIDDEN_READ,
              SessionApiExamples.forbidden(AuthPermissions.SESSION_READ))
          .addExamples(
              SessionApiExamples.FORBIDDEN_READ_OWN,
              SessionApiExamples.forbidden(AuthPermissions.SESSION_READ_OWN))
          .addExamples(
              SessionApiExamples.FORBIDDEN_READ_EITHER,
              SessionApiExamples.forbidden(
                  AuthPermissions.SESSION_READ, AuthPermissions.SESSION_READ_OWN))
          .addExamples(
              SessionApiExamples.FORBIDDEN_WRITE,
              SessionApiExamples.forbidden(AuthPermissions.SESSION_WRITE))
          .addExamples(
              SessionApiExamples.FORBIDDEN_WRITE_OWN,
              SessionApiExamples.forbidden(AuthPermissions.SESSION_WRITE_OWN))
          .addExamples(
              SessionApiExamples.FORBIDDEN_WRITE_EITHER,
              SessionApiExamples.forbidden(
                  AuthPermissions.SESSION_WRITE, AuthPermissions.SESSION_WRITE_OWN))
          .addExamples(
              SessionApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      SessionApiExamples.SAMPLE_SORT_FIELD, SessionApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      SessionApiExamples.SAMPLE_SORT_FIELD, SessionApiExamples.SORT_FIELDS)));
    };
  }
}

package com.codeclash.authservice.permission.adapter.inbound.rest;

import com.codeclash.authservice.permission.adapter.inbound.rest.dto.PermissionIdProperties;
import com.codeclash.authservice.permission.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.exception.InvalidPermissionIdException;
import com.codeclash.authservice.permission.model.exception.NullPermissionIdException;
import com.codeclash.authservice.permission.model.exception.PermissionNotFoundException;
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
public class PermissionApiExamples {

  public static final String PERMISSION_NOT_FOUND = "permission.PERMISSION_NOT_FOUND";
  public static final String INVALID_PERMISSION_ID = "permission.INVALID_PERMISSION_ID";
  public static final String NULL_PERMISSION_ID = "permission.NULL_PERMISSION_ID";
  public static final String FORBIDDEN_READ = "permission.FORBIDDEN_READ";
  public static final String UNKNOWN_SORT_FIELD = "permission.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final List<String> SORT_FIELDS = List.of("created_at", "id", "name", "updated_at");

  private static Example forbidden(String... permissions) {
    List<String> missing = List.of(permissions);
    return ApiExamples.error(
        new ForbiddenException(missing), new RequiredPermissionsProperties(missing));
  }

  @Bean
  OpenApiCustomizer permissionOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          PermissionIdProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              PermissionApiExamples.PERMISSION_NOT_FOUND,
              ApiExamples.error(
                  new PermissionNotFoundException(PermissionApiExamples.SAMPLE_ID),
                  new PermissionIdProperties(PermissionApiExamples.SAMPLE_ID)))
          .addExamples(
              PermissionApiExamples.INVALID_PERMISSION_ID,
              ApiExamples.error(
                  new InvalidPermissionIdException(PermissionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(
              PermissionApiExamples.NULL_PERMISSION_ID,
              ApiExamples.error(new NullPermissionIdException()))
          .addExamples(
              PermissionApiExamples.FORBIDDEN_READ,
              PermissionApiExamples.forbidden(AuthPermissions.PERMISSION_READ))
          .addExamples(
              PermissionApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      PermissionApiExamples.SAMPLE_SORT_FIELD, PermissionApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      PermissionApiExamples.SAMPLE_SORT_FIELD, PermissionApiExamples.SORT_FIELDS)));
    };
  }
}

package com.codeclash.authservice.role.adapter.inbound.rest;

import com.codeclash.authservice.role.adapter.inbound.rest.dto.LengthProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.MaxLengthProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.PermissionIdsProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.RoleIdProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.RoleNameProperties;
import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.exception.DefaultRoleModificationException;
import com.codeclash.authservice.role.model.exception.DescriptionTooLongException;
import com.codeclash.authservice.role.model.exception.DescriptionTooShortException;
import com.codeclash.authservice.role.model.exception.DuplicateRoleNameException;
import com.codeclash.authservice.role.model.exception.InvalidNameException;
import com.codeclash.authservice.role.model.exception.InvalidPermissionIdException;
import com.codeclash.authservice.role.model.exception.InvalidRoleIdException;
import com.codeclash.authservice.role.model.exception.NameTooLongException;
import com.codeclash.authservice.role.model.exception.NullDescriptionException;
import com.codeclash.authservice.role.model.exception.NullNameException;
import com.codeclash.authservice.role.model.exception.NullPermissionsException;
import com.codeclash.authservice.role.model.exception.NullRoleIdException;
import com.codeclash.authservice.role.model.exception.PermissionsNotFoundException;
import com.codeclash.authservice.role.model.exception.RoleNotFoundException;
import com.codeclash.authservice.role.model.permission.PermissionId;
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
public class RoleApiExamples {

  public static final String ROLE_NOT_FOUND = "role.ROLE_NOT_FOUND";
  public static final String DEFAULT_ROLE_MODIFICATION = "role.DEFAULT_ROLE_MODIFICATION";
  public static final String DUPLICATE_ROLE_NAME = "role.DUPLICATE_ROLE_NAME";
  public static final String PERMISSIONS_NOT_FOUND = "role.PERMISSIONS_NOT_FOUND";
  public static final String NAME_TOO_LONG = "role.NAME_TOO_LONG";
  public static final String INVALID_NAME = "role.INVALID_NAME";
  public static final String DESCRIPTION_TOO_SHORT = "role.DESCRIPTION_TOO_SHORT";
  public static final String DESCRIPTION_TOO_LONG = "role.DESCRIPTION_TOO_LONG";
  public static final String INVALID_ROLE_ID = "role.INVALID_ROLE_ID";
  public static final String INVALID_PERMISSION_ID = "role.INVALID_PERMISSION_ID";
  public static final String NULL_NAME = "role.NULL_NAME";
  public static final String NULL_DESCRIPTION = "role.NULL_DESCRIPTION";
  public static final String NULL_PERMISSIONS = "role.NULL_PERMISSIONS";
  public static final String NULL_ROLE_ID = "role.NULL_ROLE_ID";
  public static final String FORBIDDEN_READ = "role.FORBIDDEN_READ";
  public static final String FORBIDDEN_WRITE = "role.FORBIDDEN_WRITE";
  public static final String UNKNOWN_SORT_FIELD = "role.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final String SAMPLE_NAME = "contest_manager";
  private static final List<UUID> SAMPLE_PERMISSION_IDS =
      List.of(UUID.fromString("01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b"));
  private static final List<String> SORT_FIELDS =
      List.of("created_at", "id", "is_default", "name", "updated_at");

  private static Example forbidden(String... permissions) {
    List<String> missing = List.of(permissions);
    return ApiExamples.error(
        new ForbiddenException(missing), new RequiredPermissionsProperties(missing));
  }

  @Bean
  OpenApiCustomizer roleOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          RoleIdProperties.class,
          RoleNameProperties.class,
          PermissionIdsProperties.class,
          MaxLengthProperties.class,
          LengthProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              RoleApiExamples.ROLE_NOT_FOUND,
              ApiExamples.error(
                  new RoleNotFoundException(RoleApiExamples.SAMPLE_ID),
                  new RoleIdProperties(RoleApiExamples.SAMPLE_ID)))
          .addExamples(
              RoleApiExamples.DEFAULT_ROLE_MODIFICATION,
              ApiExamples.error(
                  new DefaultRoleModificationException(RoleApiExamples.SAMPLE_ID),
                  new RoleIdProperties(RoleApiExamples.SAMPLE_ID)))
          .addExamples(
              RoleApiExamples.DUPLICATE_ROLE_NAME,
              ApiExamples.error(
                  new DuplicateRoleNameException(RoleApiExamples.SAMPLE_NAME),
                  new RoleNameProperties(RoleApiExamples.SAMPLE_NAME)))
          .addExamples(
              RoleApiExamples.PERMISSIONS_NOT_FOUND,
              ApiExamples.error(
                  new PermissionsNotFoundException(RoleApiExamples.SAMPLE_PERMISSION_IDS),
                  new PermissionIdsProperties(RoleApiExamples.SAMPLE_PERMISSION_IDS)))
          .addExamples(
              RoleApiExamples.NAME_TOO_LONG,
              ApiExamples.error(
                  new NameTooLongException("a".repeat(Name.MAX_SIZE + 1)),
                  new MaxLengthProperties(Name.MAX_SIZE)))
          .addExamples(
              RoleApiExamples.INVALID_NAME,
              ApiExamples.error(new InvalidNameException("Contest Manager")))
          .addExamples(
              RoleApiExamples.DESCRIPTION_TOO_SHORT,
              ApiExamples.error(
                  new DescriptionTooShortException(""),
                  new LengthProperties(Description.MIN_SIZE, Description.MAX_SIZE)))
          .addExamples(
              RoleApiExamples.DESCRIPTION_TOO_LONG,
              ApiExamples.error(
                  new DescriptionTooLongException("a".repeat(Description.MAX_SIZE + 1)),
                  new LengthProperties(Description.MIN_SIZE, Description.MAX_SIZE)))
          .addExamples(
              RoleApiExamples.INVALID_ROLE_ID,
              ApiExamples.error(
                  new InvalidRoleIdException(RoleApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(
              RoleApiExamples.INVALID_PERMISSION_ID,
              ApiExamples.error(
                  new InvalidPermissionIdException(RoleApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(PermissionId.VERSION)))
          .addExamples(RoleApiExamples.NULL_NAME, ApiExamples.error(new NullNameException()))
          .addExamples(
              RoleApiExamples.NULL_DESCRIPTION,
              ApiExamples.error(new NullDescriptionException()))
          .addExamples(
              RoleApiExamples.NULL_PERMISSIONS,
              ApiExamples.error(new NullPermissionsException()))
          .addExamples(RoleApiExamples.NULL_ROLE_ID, ApiExamples.error(new NullRoleIdException()))
          .addExamples(
              RoleApiExamples.FORBIDDEN_READ,
              RoleApiExamples.forbidden(AuthPermissions.ROLE_READ))
          .addExamples(
              RoleApiExamples.FORBIDDEN_WRITE,
              RoleApiExamples.forbidden(AuthPermissions.ROLE_WRITE))
          .addExamples(
              RoleApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      RoleApiExamples.SAMPLE_SORT_FIELD, RoleApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      RoleApiExamples.SAMPLE_SORT_FIELD, RoleApiExamples.SORT_FIELDS)));
    };
  }
}

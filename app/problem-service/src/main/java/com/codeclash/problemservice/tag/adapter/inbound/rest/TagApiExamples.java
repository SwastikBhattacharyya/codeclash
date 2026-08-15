package com.codeclash.problemservice.tag.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.TagIdProperties;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.TagNameProperties;
import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Name;
import com.codeclash.problemservice.tag.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.tag.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.tag.model.exception.DuplicateTagNameException;
import com.codeclash.problemservice.tag.model.exception.InvalidNameException;
import com.codeclash.problemservice.tag.model.exception.InvalidTagIdException;
import com.codeclash.problemservice.tag.model.exception.NameTooLongException;
import com.codeclash.problemservice.tag.model.exception.NameTooShortException;
import com.codeclash.problemservice.tag.model.exception.NullDescriptionException;
import com.codeclash.problemservice.tag.model.exception.NullNameException;
import com.codeclash.problemservice.tag.model.exception.NullTagIdException;
import com.codeclash.problemservice.tag.model.exception.TagNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TagApiExamples {

  public static final String TAG_NOT_FOUND = "tag.TAG_NOT_FOUND";
  public static final String DUPLICATE_TAG_NAME = "tag.DUPLICATE_TAG_NAME";
  public static final String INVALID_TAG_ID = "tag.INVALID_TAG_ID";
  public static final String NULL_TAG_ID = "tag.NULL_TAG_ID";
  public static final String NULL_NAME = "tag.NULL_NAME";
  public static final String NAME_TOO_SHORT = "tag.NAME_TOO_SHORT";
  public static final String NAME_TOO_LONG = "tag.NAME_TOO_LONG";
  public static final String INVALID_NAME = "tag.INVALID_NAME";
  public static final String NULL_DESCRIPTION = "tag.NULL_DESCRIPTION";
  public static final String DESCRIPTION_TOO_SHORT = "tag.DESCRIPTION_TOO_SHORT";
  public static final String DESCRIPTION_TOO_LONG = "tag.DESCRIPTION_TOO_LONG";
  public static final String FORBIDDEN = "tag.FORBIDDEN";
  public static final String UNKNOWN_SORT_FIELD = "tag.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_NAME = "dynamic-programming";
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final List<String> SORT_FIELDS = List.of("created_at", "id", "name", "updated_at");

  @Bean
  OpenApiCustomizer tagOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    RangeProperties nameRange = new RangeProperties(Name.MIN_SIZE, Name.MAX_SIZE);
    RangeProperties descriptionRange =
        new RangeProperties(Description.MIN_SIZE, Description.MAX_SIZE);
    List<String> permissions = List.of(ProblemPermissions.TAG_WRITE);
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          TagIdProperties.class,
          TagNameProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              TagApiExamples.TAG_NOT_FOUND,
              ApiExamples.error(
                  new TagNotFoundException(TagApiExamples.SAMPLE_ID),
                  new TagIdProperties(TagApiExamples.SAMPLE_ID)))
          .addExamples(
              TagApiExamples.DUPLICATE_TAG_NAME,
              ApiExamples.error(
                  new DuplicateTagNameException(TagApiExamples.SAMPLE_NAME),
                  new TagNameProperties(TagApiExamples.SAMPLE_NAME)))
          .addExamples(
              TagApiExamples.INVALID_TAG_ID,
              ApiExamples.error(
                  new InvalidTagIdException(TagApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(TagApiExamples.NULL_NAME, ApiExamples.error(new NullNameException()))
          .addExamples(
              TagApiExamples.NAME_TOO_SHORT,
              ApiExamples.error(new NameTooShortException("a"), nameRange))
          .addExamples(
              TagApiExamples.NAME_TOO_LONG,
              ApiExamples.error(
                  new NameTooLongException("a".repeat(Name.MAX_SIZE + 1)), nameRange))
          .addExamples(
              TagApiExamples.INVALID_NAME,
              ApiExamples.error(new InvalidNameException("dynamic\nprogramming")))
          .addExamples(
              TagApiExamples.NULL_DESCRIPTION,
              ApiExamples.error(new NullDescriptionException()))
          .addExamples(
              TagApiExamples.DESCRIPTION_TOO_SHORT,
              ApiExamples.error(new DescriptionTooShortException(""), descriptionRange))
          .addExamples(
              TagApiExamples.DESCRIPTION_TOO_LONG,
              ApiExamples.error(
                  new DescriptionTooLongException("a".repeat(Description.MAX_SIZE + 1)),
                  descriptionRange))
          .addExamples(
              TagApiExamples.FORBIDDEN,
              ApiExamples.error(
                  new ForbiddenException(permissions),
                  new RequiredPermissionsProperties(permissions)))
          .addExamples(
              TagApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      TagApiExamples.SAMPLE_SORT_FIELD, TagApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      TagApiExamples.SAMPLE_SORT_FIELD, TagApiExamples.SORT_FIELDS)))
          .addExamples(TagApiExamples.NULL_TAG_ID, ApiExamples.error(new NullTagIdException()));
    };
  }
}

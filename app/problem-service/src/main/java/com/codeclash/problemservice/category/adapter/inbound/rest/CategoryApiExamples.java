package com.codeclash.problemservice.category.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.CategoryIdProperties;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.CategoryNameProperties;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import com.codeclash.problemservice.category.model.exception.CategoryNotFoundException;
import com.codeclash.problemservice.category.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.category.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.category.model.exception.DuplicateCategoryNameException;
import com.codeclash.problemservice.category.model.exception.InvalidCategoryIdException;
import com.codeclash.problemservice.category.model.exception.InvalidNameException;
import com.codeclash.problemservice.category.model.exception.NameTooLongException;
import com.codeclash.problemservice.category.model.exception.NameTooShortException;
import com.codeclash.problemservice.category.model.exception.NullCategoryIdException;
import com.codeclash.problemservice.category.model.exception.NullDescriptionException;
import com.codeclash.problemservice.category.model.exception.NullNameException;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CategoryApiExamples {

  public static final String CATEGORY_NOT_FOUND = "category.CATEGORY_NOT_FOUND";
  public static final String DUPLICATE_CATEGORY_NAME = "category.DUPLICATE_CATEGORY_NAME";
  public static final String INVALID_CATEGORY_ID = "category.INVALID_CATEGORY_ID";
  public static final String NULL_CATEGORY_ID = "category.NULL_CATEGORY_ID";
  public static final String NULL_NAME = "category.NULL_NAME";
  public static final String NAME_TOO_SHORT = "category.NAME_TOO_SHORT";
  public static final String NAME_TOO_LONG = "category.NAME_TOO_LONG";
  public static final String INVALID_NAME = "category.INVALID_NAME";
  public static final String NULL_DESCRIPTION = "category.NULL_DESCRIPTION";
  public static final String DESCRIPTION_TOO_SHORT = "category.DESCRIPTION_TOO_SHORT";
  public static final String DESCRIPTION_TOO_LONG = "category.DESCRIPTION_TOO_LONG";
  public static final String FORBIDDEN = "category.FORBIDDEN";
  public static final String UNKNOWN_SORT_FIELD = "category.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_NAME = "graph-theory";
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final List<String> SORT_FIELDS = List.of("created_at", "id", "name", "updated_at");

  @Bean
  OpenApiCustomizer categoryOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    RangeProperties nameRange = new RangeProperties(Name.MIN_SIZE, Name.MAX_SIZE);
    RangeProperties descriptionRange =
        new RangeProperties(Description.MIN_SIZE, Description.MAX_SIZE);
    List<String> permissions = List.of(ProblemPermissions.CATEGORY_WRITE);
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          CategoryIdProperties.class,
          CategoryNameProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              CategoryApiExamples.CATEGORY_NOT_FOUND,
              ApiExamples.error(
                  new CategoryNotFoundException(CategoryApiExamples.SAMPLE_ID),
                  new CategoryIdProperties(CategoryApiExamples.SAMPLE_ID)))
          .addExamples(
              CategoryApiExamples.DUPLICATE_CATEGORY_NAME,
              ApiExamples.error(
                  new DuplicateCategoryNameException(CategoryApiExamples.SAMPLE_NAME),
                  new CategoryNameProperties(CategoryApiExamples.SAMPLE_NAME)))
          .addExamples(
              CategoryApiExamples.INVALID_CATEGORY_ID,
              ApiExamples.error(
                  new InvalidCategoryIdException(CategoryApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(CategoryApiExamples.NULL_NAME, ApiExamples.error(new NullNameException()))
          .addExamples(
              CategoryApiExamples.NAME_TOO_SHORT,
              ApiExamples.error(new NameTooShortException("a"), nameRange))
          .addExamples(
              CategoryApiExamples.NAME_TOO_LONG,
              ApiExamples.error(
                  new NameTooLongException("a".repeat(Name.MAX_SIZE + 1)), nameRange))
          .addExamples(
              CategoryApiExamples.INVALID_NAME,
              ApiExamples.error(new InvalidNameException("graph\ntheory")))
          .addExamples(
              CategoryApiExamples.NULL_DESCRIPTION,
              ApiExamples.error(new NullDescriptionException()))
          .addExamples(
              CategoryApiExamples.DESCRIPTION_TOO_SHORT,
              ApiExamples.error(new DescriptionTooShortException(""), descriptionRange))
          .addExamples(
              CategoryApiExamples.DESCRIPTION_TOO_LONG,
              ApiExamples.error(
                  new DescriptionTooLongException("a".repeat(Description.MAX_SIZE + 1)),
                  descriptionRange))
          .addExamples(
              CategoryApiExamples.FORBIDDEN,
              ApiExamples.error(
                  new ForbiddenException(permissions),
                  new RequiredPermissionsProperties(permissions)))
          .addExamples(
              CategoryApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      CategoryApiExamples.SAMPLE_SORT_FIELD, CategoryApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      CategoryApiExamples.SAMPLE_SORT_FIELD, CategoryApiExamples.SORT_FIELDS)))
          .addExamples(
              CategoryApiExamples.NULL_CATEGORY_ID,
              ApiExamples.error(new NullCategoryIdException()));
    };
  }
}

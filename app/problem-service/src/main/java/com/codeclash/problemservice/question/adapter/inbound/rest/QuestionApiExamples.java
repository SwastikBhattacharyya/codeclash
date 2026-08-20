package com.codeclash.problemservice.question.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.CategoryIdsProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.QuestionIdProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.QuestionRunningContestProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.TagIdsProperties;
import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.category.CategoryId;
import com.codeclash.problemservice.question.model.exception.CategoriesNotFoundException;
import com.codeclash.problemservice.question.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.question.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.question.model.exception.InvalidCategoryIdException;
import com.codeclash.problemservice.question.model.exception.InvalidQuestionIdException;
import com.codeclash.problemservice.question.model.exception.InvalidTagIdException;
import com.codeclash.problemservice.question.model.exception.InvalidTitleException;
import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooLowException;
import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooLowException;
import com.codeclash.problemservice.question.model.exception.NullCategoriesException;
import com.codeclash.problemservice.question.model.exception.NullDescriptionException;
import com.codeclash.problemservice.question.model.exception.NullMaxRamLimitException;
import com.codeclash.problemservice.question.model.exception.NullMaxRunTimeException;
import com.codeclash.problemservice.question.model.exception.NullQuestionIdException;
import com.codeclash.problemservice.question.model.exception.NullSummaryException;
import com.codeclash.problemservice.question.model.exception.NullTagsException;
import com.codeclash.problemservice.question.model.exception.NullTitleException;
import com.codeclash.problemservice.question.model.exception.NullVisibleException;
import com.codeclash.problemservice.question.model.exception.QuestionInRunningContestException;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.model.exception.SummaryTooLongException;
import com.codeclash.problemservice.question.model.exception.SummaryTooShortException;
import com.codeclash.problemservice.question.model.exception.TagsNotFoundException;
import com.codeclash.problemservice.question.model.exception.TitleTooLongException;
import com.codeclash.problemservice.question.model.exception.TitleTooShortException;
import com.codeclash.problemservice.question.model.tag.TagId;
import io.swagger.v3.oas.models.examples.Example;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class QuestionApiExamples {

  public static final String QUESTION_NOT_FOUND = "question.QUESTION_NOT_FOUND";
  public static final String QUESTION_IN_RUNNING_CONTEST = "question.QUESTION_IN_RUNNING_CONTEST";
  public static final String INVALID_QUESTION_ID = "question.INVALID_QUESTION_ID";
  public static final String NULL_QUESTION_ID = "question.NULL_QUESTION_ID";
  public static final String INVALID_CATEGORY_ID = "question.INVALID_CATEGORY_ID";
  public static final String INVALID_TAG_ID = "question.INVALID_TAG_ID";
  public static final String CATEGORIES_NOT_FOUND = "question.CATEGORIES_NOT_FOUND";
  public static final String TAGS_NOT_FOUND = "question.TAGS_NOT_FOUND";
  public static final String NULL_TITLE = "question.NULL_TITLE";
  public static final String TITLE_TOO_SHORT = "question.TITLE_TOO_SHORT";
  public static final String TITLE_TOO_LONG = "question.TITLE_TOO_LONG";
  public static final String INVALID_TITLE = "question.INVALID_TITLE";
  public static final String NULL_SUMMARY = "question.NULL_SUMMARY";
  public static final String SUMMARY_TOO_SHORT = "question.SUMMARY_TOO_SHORT";
  public static final String SUMMARY_TOO_LONG = "question.SUMMARY_TOO_LONG";
  public static final String NULL_DESCRIPTION = "question.NULL_DESCRIPTION";
  public static final String DESCRIPTION_TOO_SHORT = "question.DESCRIPTION_TOO_SHORT";
  public static final String DESCRIPTION_TOO_LONG = "question.DESCRIPTION_TOO_LONG";
  public static final String NULL_VISIBLE = "question.NULL_VISIBLE";
  public static final String NULL_MAX_RUN_TIME = "question.NULL_MAX_RUN_TIME";
  public static final String MAX_RUN_TIME_TOO_LOW = "question.MAX_RUN_TIME_TOO_LOW";
  public static final String MAX_RUN_TIME_TOO_HIGH = "question.MAX_RUN_TIME_TOO_HIGH";
  public static final String NULL_MAX_RAM_LIMIT = "question.NULL_MAX_RAM_LIMIT";
  public static final String MAX_RAM_LIMIT_TOO_LOW = "question.MAX_RAM_LIMIT_TOO_LOW";
  public static final String MAX_RAM_LIMIT_TOO_HIGH = "question.MAX_RAM_LIMIT_TOO_HIGH";
  public static final String NULL_CATEGORIES = "question.NULL_CATEGORIES";
  public static final String NULL_TAGS = "question.NULL_TAGS";
  public static final String FORBIDDEN = "question.FORBIDDEN";
  public static final String FORBIDDEN_WRITE_IS_PRIVATE = "question.FORBIDDEN_WRITE_IS_PRIVATE";
  public static final String FORBIDDEN_READ_IS_PRIVATE = "question.FORBIDDEN_READ_IS_PRIVATE";
  public static final String UNKNOWN_SORT_FIELD = "question.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final List<UUID> SAMPLE_CONTEST_IDS =
      List.of(UUID.fromString("01936f40-1c2d-7e3f-8a4b-5c6d7e8f9a0b"));
  private static final List<UUID> SAMPLE_REFERENCE_IDS =
      List.of(UUID.fromString("01936f41-2d3e-7f4a-9b5c-6d7e8f9a0b1c"));
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final List<String> SORT_FIELDS =
      List.of("created_at", "id", "summary", "title", "updated_at", "visible");

  private static Example forbidden(String permission) {
    List<String> permissions = List.of(permission);
    return ApiExamples.error(
        new ForbiddenException(permissions), new RequiredPermissionsProperties(permissions));
  }

  @Bean
  OpenApiCustomizer questionOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    RangeProperties titleRange = new RangeProperties(Title.MIN_SIZE, Title.MAX_SIZE);
    RangeProperties summaryRange = new RangeProperties(Summary.MIN_SIZE, Summary.MAX_SIZE);
    RangeProperties descriptionRange =
        new RangeProperties(Description.MIN_SIZE, Description.MAX_SIZE);
    RangeProperties runTimeRange = new RangeProperties(MaxRunTime.MIN, MaxRunTime.MAX);
    RangeProperties ramLimitRange = new RangeProperties(MaxRamLimit.MIN, MaxRamLimit.MAX);
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          QuestionIdProperties.class,
          QuestionRunningContestProperties.class,
          CategoryIdsProperties.class,
          TagIdsProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              QuestionApiExamples.QUESTION_NOT_FOUND,
              ApiExamples.error(
                  new QuestionNotFoundException(QuestionApiExamples.SAMPLE_ID),
                  new QuestionIdProperties(QuestionApiExamples.SAMPLE_ID)))
          .addExamples(
              QuestionApiExamples.QUESTION_IN_RUNNING_CONTEST,
              ApiExamples.error(
                  new QuestionInRunningContestException(
                      QuestionApiExamples.SAMPLE_ID, QuestionApiExamples.SAMPLE_CONTEST_IDS),
                  new QuestionRunningContestProperties(
                      QuestionApiExamples.SAMPLE_ID, QuestionApiExamples.SAMPLE_CONTEST_IDS)))
          .addExamples(
              QuestionApiExamples.INVALID_QUESTION_ID,
              ApiExamples.error(
                  new InvalidQuestionIdException(QuestionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(
              QuestionApiExamples.INVALID_CATEGORY_ID,
              ApiExamples.error(
                  new InvalidCategoryIdException(QuestionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(CategoryId.VERSION)))
          .addExamples(
              QuestionApiExamples.INVALID_TAG_ID,
              ApiExamples.error(
                  new InvalidTagIdException(QuestionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(TagId.VERSION)))
          .addExamples(
              QuestionApiExamples.CATEGORIES_NOT_FOUND,
              ApiExamples.error(
                  new CategoriesNotFoundException(QuestionApiExamples.SAMPLE_REFERENCE_IDS),
                  new CategoryIdsProperties(QuestionApiExamples.SAMPLE_REFERENCE_IDS)))
          .addExamples(
              QuestionApiExamples.TAGS_NOT_FOUND,
              ApiExamples.error(
                  new TagsNotFoundException(QuestionApiExamples.SAMPLE_REFERENCE_IDS),
                  new TagIdsProperties(QuestionApiExamples.SAMPLE_REFERENCE_IDS)))
          .addExamples(
              QuestionApiExamples.TITLE_TOO_SHORT,
              ApiExamples.error(new TitleTooShortException("Two Sum"), titleRange))
          .addExamples(
              QuestionApiExamples.TITLE_TOO_LONG,
              ApiExamples.error(
                  new TitleTooLongException("a".repeat(Title.MAX_SIZE + 1)), titleRange))
          .addExamples(
              QuestionApiExamples.INVALID_TITLE,
              ApiExamples.error(new InvalidTitleException("Two Sum\nof Pairs")))
          .addExamples(
              QuestionApiExamples.SUMMARY_TOO_SHORT,
              ApiExamples.error(new SummaryTooShortException("Add two"), summaryRange))
          .addExamples(
              QuestionApiExamples.SUMMARY_TOO_LONG,
              ApiExamples.error(
                  new SummaryTooLongException("a".repeat(Summary.MAX_SIZE + 1)), summaryRange))
          .addExamples(
              QuestionApiExamples.DESCRIPTION_TOO_SHORT,
              ApiExamples.error(
                  new DescriptionTooShortException("Add two numbers."), descriptionRange))
          .addExamples(
              QuestionApiExamples.DESCRIPTION_TOO_LONG,
              ApiExamples.error(
                  new DescriptionTooLongException("a".repeat(Description.MAX_SIZE + 1)),
                  descriptionRange))
          .addExamples(
              QuestionApiExamples.MAX_RUN_TIME_TOO_LOW,
              ApiExamples.error(
                  new MaxRunTimeTooLowException(MaxRunTime.MIN - 1), runTimeRange))
          .addExamples(
              QuestionApiExamples.MAX_RUN_TIME_TOO_HIGH,
              ApiExamples.error(
                  new MaxRunTimeTooHighException(MaxRunTime.MAX + 1), runTimeRange))
          .addExamples(
              QuestionApiExamples.MAX_RAM_LIMIT_TOO_LOW,
              ApiExamples.error(
                  new MaxRamLimitTooLowException(MaxRamLimit.MIN - 1), ramLimitRange))
          .addExamples(
              QuestionApiExamples.MAX_RAM_LIMIT_TOO_HIGH,
              ApiExamples.error(
                  new MaxRamLimitTooHighException(MaxRamLimit.MAX + 1), ramLimitRange))
          .addExamples(
              QuestionApiExamples.NULL_TITLE,
              ApiExamples.error(new NullTitleException()))
          .addExamples(
              QuestionApiExamples.NULL_SUMMARY,
              ApiExamples.error(new NullSummaryException()))
          .addExamples(
              QuestionApiExamples.NULL_DESCRIPTION,
              ApiExamples.error(new NullDescriptionException()))
          .addExamples(
              QuestionApiExamples.NULL_VISIBLE,
              ApiExamples.error(new NullVisibleException()))
          .addExamples(
              QuestionApiExamples.NULL_MAX_RUN_TIME,
              ApiExamples.error(new NullMaxRunTimeException()))
          .addExamples(
              QuestionApiExamples.NULL_MAX_RAM_LIMIT,
              ApiExamples.error(new NullMaxRamLimitException()))
          .addExamples(
              QuestionApiExamples.NULL_CATEGORIES,
              ApiExamples.error(new NullCategoriesException()))
          .addExamples(
              QuestionApiExamples.NULL_TAGS,
              ApiExamples.error(new NullTagsException()))
          .addExamples(
              QuestionApiExamples.FORBIDDEN,
              QuestionApiExamples.forbidden(ProblemPermissions.QUESTION_WRITE))
          .addExamples(
              QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
              QuestionApiExamples.forbidden(ProblemPermissions.QUESTION_WRITE_IS_PRIVATE))
          .addExamples(
              QuestionApiExamples.FORBIDDEN_READ_IS_PRIVATE,
              QuestionApiExamples.forbidden(ProblemPermissions.QUESTION_READ_IS_PRIVATE))
          .addExamples(
              QuestionApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      QuestionApiExamples.SAMPLE_SORT_FIELD, QuestionApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      QuestionApiExamples.SAMPLE_SORT_FIELD, QuestionApiExamples.SORT_FIELDS)))
          .addExamples(
              QuestionApiExamples.NULL_QUESTION_ID,
              ApiExamples.error(new NullQuestionIdException()));
    };
  }
}

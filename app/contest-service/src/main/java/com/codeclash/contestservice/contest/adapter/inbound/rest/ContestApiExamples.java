package com.codeclash.contestservice.contest.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestEndProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestIdProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestLiveProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestNameProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.QuestionIdsProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.RegistrationProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ScheduleProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.UserIdProperties;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.exception.AlreadyRegisteredException;
import com.codeclash.contestservice.contest.model.exception.ContestAlreadyLiveException;
import com.codeclash.contestservice.contest.model.exception.ContestEndedException;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.exception.DescriptionTooLongException;
import com.codeclash.contestservice.contest.model.exception.DescriptionTooShortException;
import com.codeclash.contestservice.contest.model.exception.DuplicateContestNameException;
import com.codeclash.contestservice.contest.model.exception.DuplicateQuestionsException;
import com.codeclash.contestservice.contest.model.exception.EndAtInPastException;
import com.codeclash.contestservice.contest.model.exception.EndAtNotAfterLiveAtException;
import com.codeclash.contestservice.contest.model.exception.InvalidContestIdException;
import com.codeclash.contestservice.contest.model.exception.InvalidNameException;
import com.codeclash.contestservice.contest.model.exception.InvalidQuestionIdException;
import com.codeclash.contestservice.contest.model.exception.LiveAtInPastException;
import com.codeclash.contestservice.contest.model.exception.NameTooLongException;
import com.codeclash.contestservice.contest.model.exception.NameTooShortException;
import com.codeclash.contestservice.contest.model.exception.NullContestIdException;
import com.codeclash.contestservice.contest.model.exception.NullDescriptionException;
import com.codeclash.contestservice.contest.model.exception.NullEndAtException;
import com.codeclash.contestservice.contest.model.exception.NullLiveAtException;
import com.codeclash.contestservice.contest.model.exception.NullNameException;
import com.codeclash.contestservice.contest.model.exception.NullPointsException;
import com.codeclash.contestservice.contest.model.exception.NullQuestionIdException;
import com.codeclash.contestservice.contest.model.exception.NullQuestionsException;
import com.codeclash.contestservice.contest.model.exception.PointsTooHighException;
import com.codeclash.contestservice.contest.model.exception.PointsTooLowException;
import com.codeclash.contestservice.contest.model.exception.QuestionsNotFoundException;
import com.codeclash.contestservice.contest.model.exception.RegistrationNotFoundException;
import com.codeclash.contestservice.contest.model.exception.RegistrationUserNotFoundException;
import com.codeclash.contestservice.contest.model.question.Points;
import com.codeclash.contestservice.contest.model.question.QuestionId;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import com.codeclash.contracts.contest.ContestPermissions;
import io.swagger.v3.oas.models.examples.Example;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ContestApiExamples {

  public static final String CONTEST_NOT_FOUND = "contest.CONTEST_NOT_FOUND";
  public static final String REGISTRATION_NOT_FOUND = "contest.REGISTRATION_NOT_FOUND";
  public static final String REGISTRATION_USER_NOT_FOUND = "contest.REGISTRATION_USER_NOT_FOUND";
  public static final String DUPLICATE_CONTEST_NAME = "contest.DUPLICATE_CONTEST_NAME";
  public static final String ALREADY_REGISTERED = "contest.ALREADY_REGISTERED";
  public static final String CONTEST_ENDED = "contest.CONTEST_ENDED";
  public static final String CONTEST_ALREADY_LIVE = "contest.CONTEST_ALREADY_LIVE";
  public static final String QUESTIONS_NOT_FOUND = "contest.QUESTIONS_NOT_FOUND";
  public static final String DUPLICATE_QUESTIONS = "contest.DUPLICATE_QUESTIONS";
  public static final String NAME_TOO_SHORT = "contest.NAME_TOO_SHORT";
  public static final String NAME_TOO_LONG = "contest.NAME_TOO_LONG";
  public static final String INVALID_NAME = "contest.INVALID_NAME";
  public static final String DESCRIPTION_TOO_SHORT = "contest.DESCRIPTION_TOO_SHORT";
  public static final String DESCRIPTION_TOO_LONG = "contest.DESCRIPTION_TOO_LONG";
  public static final String POINTS_TOO_LOW = "contest.POINTS_TOO_LOW";
  public static final String POINTS_TOO_HIGH = "contest.POINTS_TOO_HIGH";
  public static final String END_AT_NOT_AFTER_LIVE_AT = "contest.END_AT_NOT_AFTER_LIVE_AT";
  public static final String LIVE_AT_IN_PAST = "contest.LIVE_AT_IN_PAST";
  public static final String END_AT_IN_PAST = "contest.END_AT_IN_PAST";
  public static final String INVALID_CONTEST_ID = "contest.INVALID_CONTEST_ID";
  public static final String INVALID_QUESTION_ID = "contest.INVALID_QUESTION_ID";
  public static final String NULL_CONTEST_ID = "contest.NULL_CONTEST_ID";
  public static final String NULL_NAME = "contest.NULL_NAME";
  public static final String NULL_DESCRIPTION = "contest.NULL_DESCRIPTION";
  public static final String NULL_LIVE_AT = "contest.NULL_LIVE_AT";
  public static final String NULL_END_AT = "contest.NULL_END_AT";
  public static final String NULL_QUESTIONS = "contest.NULL_QUESTIONS";
  public static final String NULL_QUESTION_ID = "contest.NULL_QUESTION_ID";
  public static final String NULL_POINTS = "contest.NULL_POINTS";
  public static final String FORBIDDEN_WRITE = "contest.FORBIDDEN_WRITE";
  public static final String UNKNOWN_SORT_FIELD = "contest.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final UUID SAMPLE_USER_ID =
      UUID.fromString("01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e");
  private static final String SAMPLE_NAME = "Weekly Round 12";
  private static final Instant SAMPLE_TIME = Instant.parse("2026-01-01T12:00:00Z");
  private static final List<UUID> SAMPLE_QUESTION_IDS =
      List.of(UUID.fromString("01936f42-3e4f-7a5b-8c6d-7e8f9a0b1c2d"));
  private static final List<String> SORT_FIELDS =
      List.of("created_at", "end_at", "id", "live_at", "name", "updated_at");

  private static Example forbidden(String... permissions) {
    List<String> missing = List.of(permissions);
    return ApiExamples.error(
        new ForbiddenException(missing), new RequiredPermissionsProperties(missing));
  }

  @Bean
  OpenApiCustomizer contestOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          ContestIdProperties.class,
          ContestNameProperties.class,
          RegistrationProperties.class,
          UserIdProperties.class,
          ContestEndProperties.class,
          ContestLiveProperties.class,
          QuestionIdsProperties.class,
          ScheduleProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              ContestApiExamples.CONTEST_NOT_FOUND,
              ApiExamples.error(
                  new ContestNotFoundException(ContestApiExamples.SAMPLE_ID),
                  new ContestIdProperties(ContestApiExamples.SAMPLE_ID)))
          .addExamples(
              ContestApiExamples.REGISTRATION_NOT_FOUND,
              ApiExamples.error(
                  new RegistrationNotFoundException(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_USER_ID),
                  new RegistrationProperties(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_USER_ID)))
          .addExamples(
              ContestApiExamples.REGISTRATION_USER_NOT_FOUND,
              ApiExamples.error(
                  new RegistrationUserNotFoundException(ContestApiExamples.SAMPLE_USER_ID),
                  new UserIdProperties(ContestApiExamples.SAMPLE_USER_ID)))
          .addExamples(
              ContestApiExamples.DUPLICATE_CONTEST_NAME,
              ApiExamples.error(
                  new DuplicateContestNameException(ContestApiExamples.SAMPLE_NAME),
                  new ContestNameProperties(ContestApiExamples.SAMPLE_NAME)))
          .addExamples(
              ContestApiExamples.ALREADY_REGISTERED,
              ApiExamples.error(
                  new AlreadyRegisteredException(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_USER_ID),
                  new RegistrationProperties(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_USER_ID)))
          .addExamples(
              ContestApiExamples.CONTEST_ENDED,
              ApiExamples.error(
                  new ContestEndedException(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_TIME),
                  new ContestEndProperties(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_TIME)))
          .addExamples(
              ContestApiExamples.CONTEST_ALREADY_LIVE,
              ApiExamples.error(
                  new ContestAlreadyLiveException(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_TIME),
                  new ContestLiveProperties(
                      ContestApiExamples.SAMPLE_ID, ContestApiExamples.SAMPLE_TIME)))
          .addExamples(
              ContestApiExamples.QUESTIONS_NOT_FOUND,
              ApiExamples.error(
                  new QuestionsNotFoundException(ContestApiExamples.SAMPLE_QUESTION_IDS),
                  new QuestionIdsProperties(ContestApiExamples.SAMPLE_QUESTION_IDS)))
          .addExamples(
              ContestApiExamples.DUPLICATE_QUESTIONS,
              ApiExamples.error(
                  new DuplicateQuestionsException(ContestApiExamples.SAMPLE_QUESTION_IDS),
                  new QuestionIdsProperties(ContestApiExamples.SAMPLE_QUESTION_IDS)))
          .addExamples(
              ContestApiExamples.NAME_TOO_SHORT,
              ApiExamples.error(
                  new NameTooShortException("Round 1"),
                  new RangeProperties(Name.MIN_SIZE, Name.MAX_SIZE)))
          .addExamples(
              ContestApiExamples.NAME_TOO_LONG,
              ApiExamples.error(
                  new NameTooLongException("a".repeat(Name.MAX_SIZE + 1)),
                  new RangeProperties(Name.MIN_SIZE, Name.MAX_SIZE)))
          .addExamples(
              ContestApiExamples.INVALID_NAME,
              ApiExamples.error(new InvalidNameException("Weekly\nRound 12")))
          .addExamples(
              ContestApiExamples.DESCRIPTION_TOO_SHORT,
              ApiExamples.error(
                  new DescriptionTooShortException("Short contest."),
                  new RangeProperties(Description.MIN_SIZE, Description.MAX_SIZE)))
          .addExamples(
              ContestApiExamples.DESCRIPTION_TOO_LONG,
              ApiExamples.error(
                  new DescriptionTooLongException("a".repeat(Description.MAX_SIZE + 1)),
                  new RangeProperties(Description.MIN_SIZE, Description.MAX_SIZE)))
          .addExamples(
              ContestApiExamples.POINTS_TOO_LOW,
              ApiExamples.error(
                  new PointsTooLowException(Points.MIN - 1),
                  new RangeProperties(Points.MIN, Points.MAX)))
          .addExamples(
              ContestApiExamples.POINTS_TOO_HIGH,
              ApiExamples.error(
                  new PointsTooHighException(Points.MAX + 1),
                  new RangeProperties(Points.MIN, Points.MAX)))
          .addExamples(
              ContestApiExamples.END_AT_NOT_AFTER_LIVE_AT,
              ApiExamples.error(
                  new EndAtNotAfterLiveAtException(
                      ContestApiExamples.SAMPLE_TIME, ContestApiExamples.SAMPLE_TIME),
                  new ScheduleProperties(
                      ContestApiExamples.SAMPLE_TIME, ContestApiExamples.SAMPLE_TIME)))
          .addExamples(
              ContestApiExamples.LIVE_AT_IN_PAST,
              ApiExamples.error(new LiveAtInPastException(ContestApiExamples.SAMPLE_TIME)))
          .addExamples(
              ContestApiExamples.END_AT_IN_PAST,
              ApiExamples.error(new EndAtInPastException(ContestApiExamples.SAMPLE_TIME)))
          .addExamples(
              ContestApiExamples.INVALID_CONTEST_ID,
              ApiExamples.error(
                  new InvalidContestIdException(ContestApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(
              ContestApiExamples.INVALID_QUESTION_ID,
              ApiExamples.error(
                  new InvalidQuestionIdException(ContestApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(QuestionId.VERSION)))
          .addExamples(
              ContestApiExamples.NULL_CONTEST_ID,
              ApiExamples.error(new NullContestIdException()))
          .addExamples(ContestApiExamples.NULL_NAME, ApiExamples.error(new NullNameException()))
          .addExamples(
              ContestApiExamples.NULL_DESCRIPTION,
              ApiExamples.error(new NullDescriptionException()))
          .addExamples(
              ContestApiExamples.NULL_LIVE_AT,
              ApiExamples.error(new NullLiveAtException()))
          .addExamples(ContestApiExamples.NULL_END_AT, ApiExamples.error(new NullEndAtException()))
          .addExamples(
              ContestApiExamples.NULL_QUESTIONS,
              ApiExamples.error(new NullQuestionsException()))
          .addExamples(
              ContestApiExamples.NULL_QUESTION_ID,
              ApiExamples.error(new NullQuestionIdException()))
          .addExamples(ContestApiExamples.NULL_POINTS, ApiExamples.error(new NullPointsException()))
          .addExamples(
              ContestApiExamples.FORBIDDEN_WRITE,
              ContestApiExamples.forbidden(ContestPermissions.CONTEST_WRITE))
          .addExamples(
              ContestApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      ContestApiExamples.SAMPLE_SORT_FIELD, ContestApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      ContestApiExamples.SAMPLE_SORT_FIELD, ContestApiExamples.SORT_FIELDS)));
    };
  }
}

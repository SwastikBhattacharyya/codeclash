package com.codeclash.problemservice.submission.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.AllowedValuesProperties;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.adapter.inbound.rest.dto.MaxLengthProperties;
import com.codeclash.problemservice.submission.adapter.inbound.rest.dto.QuestionIdProperties;
import com.codeclash.problemservice.submission.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.submission.adapter.inbound.rest.dto.SubmissionIdProperties;
import com.codeclash.problemservice.submission.adapter.inbound.rest.dto.UserIdProperties;
import com.codeclash.problemservice.submission.model.Code;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.UserId;
import com.codeclash.problemservice.submission.model.exception.BlankCodeException;
import com.codeclash.problemservice.submission.model.exception.CodeTooLongException;
import com.codeclash.problemservice.submission.model.exception.InvalidLanguageException;
import com.codeclash.problemservice.submission.model.exception.InvalidQuestionIdException;
import com.codeclash.problemservice.submission.model.exception.InvalidSubmissionIdException;
import com.codeclash.problemservice.submission.model.exception.InvalidSubmissionStateException;
import com.codeclash.problemservice.submission.model.exception.InvalidUserIdException;
import com.codeclash.problemservice.submission.model.exception.NullCodeException;
import com.codeclash.problemservice.submission.model.exception.NullLanguageException;
import com.codeclash.problemservice.submission.model.exception.NullQuestionIdException;
import com.codeclash.problemservice.submission.model.exception.NullSubmissionIdException;
import com.codeclash.problemservice.submission.model.exception.NullUserIdException;
import com.codeclash.problemservice.submission.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.submission.model.exception.SubmissionNotFoundException;
import com.codeclash.problemservice.submission.model.exception.UserNotFoundException;
import io.swagger.v3.oas.models.examples.Example;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SubmissionApiExamples {

  public static final String SUBMISSION_NOT_FOUND = "submission.SUBMISSION_NOT_FOUND";
  public static final String INVALID_SUBMISSION_ID = "submission.INVALID_SUBMISSION_ID";
  public static final String NULL_SUBMISSION_ID = "submission.NULL_SUBMISSION_ID";
  public static final String INVALID_QUESTION_ID = "submission.INVALID_QUESTION_ID";
  public static final String NULL_QUESTION_ID = "submission.NULL_QUESTION_ID";
  public static final String INVALID_USER_ID = "submission.INVALID_USER_ID";
  public static final String NULL_USER_ID = "submission.NULL_USER_ID";
  public static final String NULL_LANGUAGE = "submission.NULL_LANGUAGE";
  public static final String INVALID_LANGUAGE = "submission.INVALID_LANGUAGE";
  public static final String INVALID_SUBMISSION_STATE = "submission.INVALID_SUBMISSION_STATE";
  public static final String NULL_CODE = "submission.NULL_CODE";
  public static final String BLANK_CODE = "submission.BLANK_CODE";
  public static final String CODE_TOO_LONG = "submission.CODE_TOO_LONG";
  public static final String QUESTION_NOT_FOUND = "submission.QUESTION_NOT_FOUND";
  public static final String USER_NOT_FOUND = "submission.USER_NOT_FOUND";
  public static final String FORBIDDEN_WRITE = "submission.FORBIDDEN_WRITE";
  public static final String FORBIDDEN_QUESTION_PRIVATE = "submission.FORBIDDEN_QUESTION_PRIVATE";
  public static final String FORBIDDEN_DELETE = "submission.FORBIDDEN_DELETE";
  public static final String FORBIDDEN_DELETE_OWN = "submission.FORBIDDEN_DELETE_OWN";
  public static final String UNKNOWN_SORT_FIELD = "submission.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final List<String> SORT_FIELDS =
      List.of("created_at", "id", "language", "state", "updated_at");

  private static Example forbidden(String... permissions) {
    List<String> missing = List.of(permissions);
    return ApiExamples.error(
        new ForbiddenException(missing), new RequiredPermissionsProperties(missing));
  }

  @Bean
  OpenApiCustomizer submissionOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          SubmissionIdProperties.class,
          QuestionIdProperties.class,
          UserIdProperties.class,
          RequiredVersionProperties.class,
          MaxLengthProperties.class);
      openApi
          .getComponents()
          .addExamples(
              SubmissionApiExamples.SUBMISSION_NOT_FOUND,
              ApiExamples.error(
                  new SubmissionNotFoundException(SubmissionApiExamples.SAMPLE_ID),
                  new SubmissionIdProperties(SubmissionApiExamples.SAMPLE_ID)))
          .addExamples(
              SubmissionApiExamples.INVALID_SUBMISSION_ID,
              ApiExamples.error(
                  new InvalidSubmissionIdException(SubmissionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(
              SubmissionApiExamples.NULL_SUBMISSION_ID,
              ApiExamples.error(new NullSubmissionIdException()))
          .addExamples(
              SubmissionApiExamples.INVALID_QUESTION_ID,
              ApiExamples.error(
                  new InvalidQuestionIdException(SubmissionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(QuestionId.VERSION)))
          .addExamples(
              SubmissionApiExamples.NULL_QUESTION_ID,
              ApiExamples.error(new NullQuestionIdException()))
          .addExamples(
              SubmissionApiExamples.INVALID_USER_ID,
              ApiExamples.error(
                  new InvalidUserIdException(SubmissionApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(UserId.VERSION)))
          .addExamples(
              SubmissionApiExamples.NULL_USER_ID,
              ApiExamples.error(new NullUserIdException()))
          .addExamples(
              SubmissionApiExamples.NULL_LANGUAGE,
              ApiExamples.error(new NullLanguageException()))
          .addExamples(
              SubmissionApiExamples.INVALID_LANGUAGE,
              ApiExamples.error(
                  new InvalidLanguageException("rust"),
                  new AllowedValuesProperties(Language.allowed())))
          .addExamples(
              SubmissionApiExamples.INVALID_SUBMISSION_STATE,
              ApiExamples.error(
                  new InvalidSubmissionStateException("done"),
                  new AllowedValuesProperties(SubmissionState.allowed())))
          .addExamples(SubmissionApiExamples.NULL_CODE, ApiExamples.error(new NullCodeException()))
          .addExamples(
              SubmissionApiExamples.BLANK_CODE,
              ApiExamples.error(new BlankCodeException()))
          .addExamples(
              SubmissionApiExamples.CODE_TOO_LONG,
              ApiExamples.error(new CodeTooLongException(), new MaxLengthProperties(Code.MAX_SIZE)))
          .addExamples(
              SubmissionApiExamples.QUESTION_NOT_FOUND,
              ApiExamples.error(
                  new QuestionNotFoundException(SubmissionApiExamples.SAMPLE_ID),
                  new QuestionIdProperties(SubmissionApiExamples.SAMPLE_ID)))
          .addExamples(
              SubmissionApiExamples.USER_NOT_FOUND,
              ApiExamples.error(
                  new UserNotFoundException(SubmissionApiExamples.SAMPLE_ID),
                  new UserIdProperties(SubmissionApiExamples.SAMPLE_ID)))
          .addExamples(
              SubmissionApiExamples.FORBIDDEN_WRITE,
              SubmissionApiExamples.forbidden(ProblemPermissions.SUBMISSION_WRITE))
          .addExamples(
              SubmissionApiExamples.FORBIDDEN_QUESTION_PRIVATE,
              SubmissionApiExamples.forbidden(ProblemPermissions.QUESTION_READ_IS_PRIVATE))
          .addExamples(
              SubmissionApiExamples.FORBIDDEN_DELETE,
              SubmissionApiExamples.forbidden(
                  ProblemPermissions.SUBMISSION_DELETE, ProblemPermissions.SUBMISSION_DELETE_OWN))
          .addExamples(
              SubmissionApiExamples.FORBIDDEN_DELETE_OWN,
              SubmissionApiExamples.forbidden(ProblemPermissions.SUBMISSION_DELETE_OWN))
          .addExamples(
              SubmissionApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      SubmissionApiExamples.SAMPLE_SORT_FIELD, SubmissionApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      SubmissionApiExamples.SAMPLE_SORT_FIELD, SubmissionApiExamples.SORT_FIELDS)));
    };
  }
}

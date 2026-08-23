package com.codeclash.problemservice.testcase.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.MaxLengthProperties;
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.QuestionIdProperties;
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.TestCaseIdProperties;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.exception.InputTooLongException;
import com.codeclash.problemservice.testcase.model.exception.InvalidQuestionIdException;
import com.codeclash.problemservice.testcase.model.exception.InvalidTestCaseIdException;
import com.codeclash.problemservice.testcase.model.exception.NullInputException;
import com.codeclash.problemservice.testcase.model.exception.NullOutputException;
import com.codeclash.problemservice.testcase.model.exception.NullPublicException;
import com.codeclash.problemservice.testcase.model.exception.NullTestCaseIdException;
import com.codeclash.problemservice.testcase.model.exception.OutputTooLongException;
import com.codeclash.problemservice.testcase.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.testcase.model.exception.TestCaseNotFoundException;
import io.swagger.v3.oas.models.examples.Example;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TestCaseApiExamples {

  public static final String QUESTION_NOT_FOUND = "test_case.QUESTION_NOT_FOUND";
  public static final String TEST_CASE_NOT_FOUND = "test_case.TEST_CASE_NOT_FOUND";
  public static final String INVALID_QUESTION_ID = "test_case.INVALID_QUESTION_ID";
  public static final String INVALID_TEST_CASE_ID = "test_case.INVALID_TEST_CASE_ID";
  public static final String NULL_TEST_CASE_ID = "test_case.NULL_TEST_CASE_ID";
  public static final String NULL_INPUT = "test_case.NULL_INPUT";
  public static final String INPUT_TOO_LONG = "test_case.INPUT_TOO_LONG";
  public static final String NULL_OUTPUT = "test_case.NULL_OUTPUT";
  public static final String OUTPUT_TOO_LONG = "test_case.OUTPUT_TOO_LONG";
  public static final String NULL_PUBLIC = "test_case.NULL_PUBLIC";
  public static final String FORBIDDEN_WRITE = "test_case.FORBIDDEN_WRITE";
  public static final String FORBIDDEN_QUESTION_PRIVATE = "test_case.FORBIDDEN_QUESTION_PRIVATE";
  public static final String FORBIDDEN_READ_IS_PRIVATE = "test_case.FORBIDDEN_READ_IS_PRIVATE";
  public static final String FORBIDDEN_WRITE_IS_PRIVATE = "test_case.FORBIDDEN_WRITE_IS_PRIVATE";
  public static final String FORBIDDEN_QUESTION_AND_READ_IS_PRIVATE =
      "test_case.FORBIDDEN_QUESTION_AND_READ_IS_PRIVATE";
  public static final String FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE =
      "test_case.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE";
  public static final String UNKNOWN_SORT_FIELD = "test_case.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final List<String> SORT_FIELDS =
      List.of("created_at", "id", "is_public", "updated_at");

  private static Example forbidden(String... permissions) {
    List<String> missing = List.of(permissions);
    return ApiExamples.error(
        new ForbiddenException(missing), new RequiredPermissionsProperties(missing));
  }

  @Bean
  OpenApiCustomizer testCaseOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          TestCaseIdProperties.class,
          QuestionIdProperties.class,
          RequiredVersionProperties.class,
          MaxLengthProperties.class);
      openApi
          .getComponents()
          .addExamples(
              TestCaseApiExamples.QUESTION_NOT_FOUND,
              ApiExamples.error(
                  new QuestionNotFoundException(TestCaseApiExamples.SAMPLE_ID),
                  new QuestionIdProperties(TestCaseApiExamples.SAMPLE_ID)))
          .addExamples(
              TestCaseApiExamples.TEST_CASE_NOT_FOUND,
              ApiExamples.error(
                  new TestCaseNotFoundException(TestCaseApiExamples.SAMPLE_ID),
                  new TestCaseIdProperties(TestCaseApiExamples.SAMPLE_ID)))
          .addExamples(
              TestCaseApiExamples.INVALID_QUESTION_ID,
              ApiExamples.error(
                  new InvalidQuestionIdException(TestCaseApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(QuestionId.VERSION)))
          .addExamples(
              TestCaseApiExamples.INVALID_TEST_CASE_ID,
              ApiExamples.error(
                  new InvalidTestCaseIdException(TestCaseApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(
              TestCaseApiExamples.NULL_TEST_CASE_ID,
              ApiExamples.error(new NullTestCaseIdException()))
          .addExamples(TestCaseApiExamples.NULL_INPUT, ApiExamples.error(new NullInputException()))
          .addExamples(
              TestCaseApiExamples.INPUT_TOO_LONG,
              ApiExamples.error(
                  new InputTooLongException(),
                  new MaxLengthProperties(Input.MAX_SIZE)))
          .addExamples(
              TestCaseApiExamples.NULL_OUTPUT,
              ApiExamples.error(new NullOutputException()))
          .addExamples(
              TestCaseApiExamples.OUTPUT_TOO_LONG,
              ApiExamples.error(
                  new OutputTooLongException(),
                  new MaxLengthProperties(Output.MAX_SIZE)))
          .addExamples(
              TestCaseApiExamples.NULL_PUBLIC,
              ApiExamples.error(new NullPublicException()))
          .addExamples(
              TestCaseApiExamples.FORBIDDEN_WRITE,
              TestCaseApiExamples.forbidden(ProblemPermissions.TEST_CASE_WRITE))
          .addExamples(
              TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE,
              TestCaseApiExamples.forbidden(ProblemPermissions.QUESTION_READ_IS_PRIVATE))
          .addExamples(
              TestCaseApiExamples.FORBIDDEN_READ_IS_PRIVATE,
              TestCaseApiExamples.forbidden(ProblemPermissions.TEST_CASE_READ_IS_PRIVATE))
          .addExamples(
              TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
              TestCaseApiExamples.forbidden(ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE))
          .addExamples(
              TestCaseApiExamples.FORBIDDEN_QUESTION_AND_READ_IS_PRIVATE,
              TestCaseApiExamples.forbidden(
                  ProblemPermissions.QUESTION_READ_IS_PRIVATE,
                  ProblemPermissions.TEST_CASE_READ_IS_PRIVATE))
          .addExamples(
              TestCaseApiExamples.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE,
              TestCaseApiExamples.forbidden(
                  ProblemPermissions.QUESTION_READ_IS_PRIVATE,
                  ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE))
          .addExamples(
              TestCaseApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      TestCaseApiExamples.SAMPLE_SORT_FIELD, TestCaseApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      TestCaseApiExamples.SAMPLE_SORT_FIELD, TestCaseApiExamples.SORT_FIELDS)));
    };
  }
}

package com.codeclash.problemservice.testcase.adapter.inbound.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.rest.ErrorResponseBody;
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
import com.codeclash.problemservice.testcase.model.exception.NullQuestionIdException;
import com.codeclash.problemservice.testcase.model.exception.NullTestCaseIdException;
import com.codeclash.problemservice.testcase.model.exception.OutputTooLongException;
import com.codeclash.problemservice.testcase.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.testcase.model.exception.TestCaseNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestCaseRestExceptionHandler {

  @ExceptionHandler(QuestionNotFoundException.class)
  ResponseEntity<ErrorResponseBody<QuestionIdProperties>> handleQuestionNotFound(
      QuestionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new QuestionIdProperties(exception.getId())));
  }

  @ExceptionHandler(TestCaseNotFoundException.class)
  ResponseEntity<ErrorResponseBody<TestCaseIdProperties>> handleTestCaseNotFound(
      TestCaseNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new TestCaseIdProperties(exception.getId())));
  }

  @ExceptionHandler(InputTooLongException.class)
  ResponseEntity<ErrorResponseBody<MaxLengthProperties>> handleInputTooLong(
      InputTooLongException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new MaxLengthProperties(Input.MAX_SIZE)));
  }

  @ExceptionHandler(OutputTooLongException.class)
  ResponseEntity<ErrorResponseBody<MaxLengthProperties>> handleOutputTooLong(
      OutputTooLongException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new MaxLengthProperties(Output.MAX_SIZE)));
  }

  @ExceptionHandler(InvalidTestCaseIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidTestCaseId(
      InvalidTestCaseIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler(InvalidQuestionIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidQuestionId(
      InvalidQuestionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(QuestionId.VERSION)));
  }

  @ExceptionHandler({
    NullInputException.class,
    NullOutputException.class,
    NullPublicException.class,
    NullQuestionIdException.class,
    NullTestCaseIdException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

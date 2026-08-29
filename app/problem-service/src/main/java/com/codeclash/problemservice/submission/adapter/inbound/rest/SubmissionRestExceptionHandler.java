package com.codeclash.problemservice.submission.adapter.inbound.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.rest.dto.AllowedValuesProperties;
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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SubmissionRestExceptionHandler {

  @ExceptionHandler(QuestionNotFoundException.class)
  ResponseEntity<ErrorResponseBody<QuestionIdProperties>> handleQuestionNotFound(
      QuestionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new QuestionIdProperties(exception.getId())));
  }

  @ExceptionHandler(UserNotFoundException.class)
  ResponseEntity<ErrorResponseBody<UserIdProperties>> handleUserNotFound(
      UserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new UserIdProperties(exception.getId())));
  }

  @ExceptionHandler(SubmissionNotFoundException.class)
  ResponseEntity<ErrorResponseBody<SubmissionIdProperties>> handleSubmissionNotFound(
      SubmissionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new SubmissionIdProperties(exception.getId())));
  }

  @ExceptionHandler(InvalidLanguageException.class)
  ResponseEntity<ErrorResponseBody<AllowedValuesProperties>> handleInvalidLanguage(
      InvalidLanguageException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new AllowedValuesProperties(Language.allowed())));
  }

  @ExceptionHandler(InvalidSubmissionStateException.class)
  ResponseEntity<ErrorResponseBody<AllowedValuesProperties>> handleInvalidSubmissionState(
      InvalidSubmissionStateException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new AllowedValuesProperties(SubmissionState.allowed())));
  }

  @ExceptionHandler(CodeTooLongException.class)
  ResponseEntity<ErrorResponseBody<MaxLengthProperties>> handleCodeTooLong(
      CodeTooLongException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new MaxLengthProperties(Code.MAX_SIZE)));
  }

  @ExceptionHandler(InvalidSubmissionIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidSubmissionId(
      InvalidSubmissionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler(InvalidQuestionIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidQuestionId(
      InvalidQuestionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(QuestionId.VERSION)));
  }

  @ExceptionHandler(InvalidUserIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidUserId(
      InvalidUserIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(UserId.VERSION)));
  }

  @ExceptionHandler({
    BlankCodeException.class,
    NullCodeException.class,
    NullLanguageException.class,
    NullQuestionIdException.class,
    NullSubmissionIdException.class,
    NullUserIdException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

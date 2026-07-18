package com.codeclash.authservice.session.adapter.inbound.rest;

import com.codeclash.authservice.session.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.session.adapter.inbound.rest.dto.SessionIdProperties;
import com.codeclash.authservice.session.adapter.inbound.rest.dto.UserIdProperties;
import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.authservice.session.model.exception.AmbiguousCredentialsException;
import com.codeclash.authservice.session.model.exception.InvalidCredentialsException;
import com.codeclash.authservice.session.model.exception.InvalidSessionIdException;
import com.codeclash.authservice.session.model.exception.InvalidUserIdException;
import com.codeclash.authservice.session.model.exception.NullCredentialsException;
import com.codeclash.authservice.session.model.exception.NullLastUsedAtException;
import com.codeclash.authservice.session.model.exception.NullSessionIdException;
import com.codeclash.authservice.session.model.exception.NullUserIdException;
import com.codeclash.authservice.session.model.exception.SessionNotFoundException;
import com.codeclash.authservice.session.model.exception.UnverifiedUserException;
import com.codeclash.authservice.session.model.exception.UserNotFoundException;
import com.codeclash.common.exception.CodedException;
import com.codeclash.common.rest.ErrorResponseBody;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SessionRestExceptionHandler {

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<ErrorResponseBody<Void>> handleInvalidCredentials(
      InvalidCredentialsException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponseBody.of(exception));
  }

  @ExceptionHandler(UnverifiedUserException.class)
  ResponseEntity<ErrorResponseBody<Void>> handleUnverifiedUser(UnverifiedUserException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponseBody.of(exception));
  }

  @ExceptionHandler(SessionNotFoundException.class)
  ResponseEntity<ErrorResponseBody<SessionIdProperties>> handleSessionNotFound(
      SessionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new SessionIdProperties(exception.getId())));
  }

  @ExceptionHandler(UserNotFoundException.class)
  ResponseEntity<ErrorResponseBody<UserIdProperties>> handleUserNotFound(
      UserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new UserIdProperties(exception.getUserId())));
  }

  @ExceptionHandler(InvalidSessionIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidSessionId(
      InvalidSessionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler(InvalidUserIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidUserId(
      InvalidUserIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(UserId.VERSION)));
  }

  @ExceptionHandler({
    AmbiguousCredentialsException.class,
    NullCredentialsException.class,
    NullLastUsedAtException.class,
    NullSessionIdException.class,
    NullUserIdException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

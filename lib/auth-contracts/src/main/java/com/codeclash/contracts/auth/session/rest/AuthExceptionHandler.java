package com.codeclash.contracts.auth.session.rest;

import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.contracts.auth.session.BearerSessionFilter;
import com.codeclash.contracts.auth.session.exception.AuthUnavailableException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.exception.UnauthenticatedException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

  @ExceptionHandler(UnauthenticatedException.class)
  ResponseEntity<ErrorResponseBody<Void>> handleUnauthenticated(
      UnauthenticatedException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(HttpHeaders.WWW_AUTHENTICATE, BearerSessionFilter.SCHEME)
        .body(ErrorResponseBody.of(exception));
  }

  @ExceptionHandler(ForbiddenException.class)
  ResponseEntity<ErrorResponseBody<RequiredPermissionsProperties>> handleForbidden(
      ForbiddenException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            ErrorResponseBody.of(
                exception, new RequiredPermissionsProperties(exception.getPermissions())));
  }

  @ExceptionHandler(AuthUnavailableException.class)
  ResponseEntity<ErrorResponseBody<Void>> handleAuthUnavailable(
      AuthUnavailableException exception) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ErrorResponseBody.of(exception));
  }
}

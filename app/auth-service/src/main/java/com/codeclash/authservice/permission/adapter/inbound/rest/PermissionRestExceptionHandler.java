package com.codeclash.authservice.permission.adapter.inbound.rest;

import com.codeclash.authservice.permission.adapter.inbound.rest.dto.PermissionIdProperties;
import com.codeclash.authservice.permission.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.exception.InvalidPermissionIdException;
import com.codeclash.authservice.permission.model.exception.NullPermissionIdException;
import com.codeclash.authservice.permission.model.exception.PermissionNotFoundException;
import com.codeclash.common.rest.ErrorResponseBody;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PermissionRestExceptionHandler {

  @ExceptionHandler(PermissionNotFoundException.class)
  ResponseEntity<ErrorResponseBody<PermissionIdProperties>> handlePermissionNotFound(
      PermissionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new PermissionIdProperties(exception.getId())));
  }

  @ExceptionHandler(InvalidPermissionIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidPermissionId(
      InvalidPermissionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler(NullPermissionIdException.class)
  ResponseEntity<ErrorResponseBody<Void>> handleNullPermissionId(
      NullPermissionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

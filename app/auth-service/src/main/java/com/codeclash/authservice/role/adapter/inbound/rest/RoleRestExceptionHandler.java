package com.codeclash.authservice.role.adapter.inbound.rest;

import com.codeclash.authservice.role.adapter.inbound.rest.dto.LengthProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.MaxLengthProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.PermissionIdsProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.RoleIdProperties;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.RoleNameProperties;
import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.exception.DefaultRoleModificationException;
import com.codeclash.authservice.role.model.exception.DescriptionTooLongException;
import com.codeclash.authservice.role.model.exception.DescriptionTooShortException;
import com.codeclash.authservice.role.model.exception.DuplicateRoleNameException;
import com.codeclash.authservice.role.model.exception.InvalidNameException;
import com.codeclash.authservice.role.model.exception.InvalidPermissionIdException;
import com.codeclash.authservice.role.model.exception.InvalidRoleIdException;
import com.codeclash.authservice.role.model.exception.NameTooLongException;
import com.codeclash.authservice.role.model.exception.NullDescriptionException;
import com.codeclash.authservice.role.model.exception.NullNameException;
import com.codeclash.authservice.role.model.exception.NullPermissionsException;
import com.codeclash.authservice.role.model.exception.NullRoleIdException;
import com.codeclash.authservice.role.model.exception.PermissionsNotFoundException;
import com.codeclash.authservice.role.model.exception.RoleNotFoundException;
import com.codeclash.authservice.role.model.permission.PermissionId;
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
public class RoleRestExceptionHandler {

  @ExceptionHandler(RoleNotFoundException.class)
  ResponseEntity<ErrorResponseBody<RoleIdProperties>> handleRoleNotFound(
      RoleNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new RoleIdProperties(exception.getId())));
  }

  @ExceptionHandler(DefaultRoleModificationException.class)
  ResponseEntity<ErrorResponseBody<RoleIdProperties>> handleDefaultRoleModification(
      DefaultRoleModificationException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseBody.of(exception, new RoleIdProperties(exception.getId())));
  }

  @ExceptionHandler(DuplicateRoleNameException.class)
  ResponseEntity<ErrorResponseBody<RoleNameProperties>> handleDuplicateRoleName(
      DuplicateRoleNameException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseBody.of(exception, new RoleNameProperties(exception.getName())));
  }

  @ExceptionHandler(PermissionsNotFoundException.class)
  ResponseEntity<ErrorResponseBody<PermissionIdsProperties>> handlePermissionsNotFound(
      PermissionsNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new PermissionIdsProperties(exception.getIds())));
  }

  @ExceptionHandler(NameTooLongException.class)
  ResponseEntity<ErrorResponseBody<MaxLengthProperties>> handleNameTooLong(
      NameTooLongException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new MaxLengthProperties(Name.MAX_SIZE)));
  }

  @ExceptionHandler({DescriptionTooShortException.class, DescriptionTooLongException.class})
  ResponseEntity<ErrorResponseBody<LengthProperties>> handleDescriptionLength(
      CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new LengthProperties(Description.MIN_SIZE, Description.MAX_SIZE)));
  }

  @ExceptionHandler(InvalidRoleIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidRoleId(
      InvalidRoleIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler(InvalidPermissionIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidPermissionId(
      InvalidPermissionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(PermissionId.VERSION)));
  }

  @ExceptionHandler({
    InvalidNameException.class,
    NullDescriptionException.class,
    NullNameException.class,
    NullPermissionsException.class,
    NullRoleIdException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

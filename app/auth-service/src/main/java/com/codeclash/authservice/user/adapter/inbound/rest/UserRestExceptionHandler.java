package com.codeclash.authservice.user.adapter.inbound.rest;

import com.codeclash.authservice.user.adapter.inbound.rest.dto.EmailProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.LengthProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.MaxLengthProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.MinLettersProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.RoleIdProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.UserIdProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.UsernameProperties;
import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.Name;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.exception.DuplicateEmailException;
import com.codeclash.authservice.user.model.exception.DuplicateUsernameException;
import com.codeclash.authservice.user.model.exception.InvalidUsernameException;
import com.codeclash.authservice.user.model.exception.NullUsernameException;
import com.codeclash.authservice.user.model.exception.UsernameTooLongException;
import com.codeclash.authservice.user.model.exception.UsernameTooShortException;
import com.codeclash.authservice.user.model.exception.EmailTooLongException;
import com.codeclash.authservice.user.model.exception.InvalidEmailException;
import com.codeclash.authservice.user.model.exception.InvalidNameException;
import com.codeclash.authservice.user.model.exception.InvalidRoleIdException;
import com.codeclash.authservice.user.model.exception.InvalidUserIdException;
import com.codeclash.authservice.user.model.exception.InvalidVerificationTokenException;
import com.codeclash.authservice.user.model.exception.NameTooLongException;
import com.codeclash.authservice.user.model.exception.NameTooShortException;
import com.codeclash.authservice.user.model.exception.NullEmailException;
import com.codeclash.authservice.user.model.exception.NullNameException;
import com.codeclash.authservice.user.model.exception.NullPasswordException;
import com.codeclash.authservice.user.model.exception.NullRoleException;
import com.codeclash.authservice.user.model.exception.NullUserIdException;
import com.codeclash.authservice.user.model.exception.NullVerifiedException;
import com.codeclash.authservice.user.model.exception.PasswordMissingDigitException;
import com.codeclash.authservice.user.model.exception.PasswordMissingLowercaseException;
import com.codeclash.authservice.user.model.exception.PasswordMissingSpecialCharacterException;
import com.codeclash.authservice.user.model.exception.PasswordMissingUppercaseException;
import com.codeclash.authservice.user.model.exception.PasswordTooLongException;
import com.codeclash.authservice.user.model.exception.PasswordTooShortException;
import com.codeclash.authservice.user.model.exception.RoleNotFoundException;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.model.exception.VerificationTokenMismatchException;
import com.codeclash.authservice.user.model.password.Password;
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
public class UserRestExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  ResponseEntity<ErrorResponseBody<UserIdProperties>> handleUserNotFound(
      UserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new UserIdProperties(exception.getId())));
  }

  @ExceptionHandler(DuplicateEmailException.class)
  ResponseEntity<ErrorResponseBody<EmailProperties>> handleDuplicateEmail(
      DuplicateEmailException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseBody.of(exception, new EmailProperties(exception.getEmail())));
  }

  @ExceptionHandler(DuplicateUsernameException.class)
  ResponseEntity<ErrorResponseBody<UsernameProperties>> handleDuplicateUsername(
      DuplicateUsernameException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseBody.of(exception, new UsernameProperties(exception.getUsername())));
  }

  @ExceptionHandler({UsernameTooShortException.class, UsernameTooLongException.class})
  ResponseEntity<ErrorResponseBody<LengthProperties>> handleUsernameLength(
      CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new LengthProperties(Username.MIN_SIZE, Username.MAX_SIZE)));
  }

  @ExceptionHandler({NameTooShortException.class, NameTooLongException.class})
  ResponseEntity<ErrorResponseBody<LengthProperties>> handleNameLength(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new LengthProperties(Name.MIN_SIZE, Name.MAX_SIZE)));
  }

  @ExceptionHandler({PasswordTooShortException.class, PasswordTooLongException.class})
  ResponseEntity<ErrorResponseBody<LengthProperties>> handlePasswordLength(
      CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new LengthProperties(Password.MIN_SIZE, Password.MAX_SIZE)));
  }

  @ExceptionHandler(EmailTooLongException.class)
  ResponseEntity<ErrorResponseBody<MaxLengthProperties>> handleEmailTooLong(
      EmailTooLongException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new MaxLengthProperties(Email.MAX_SIZE)));
  }

  @ExceptionHandler(InvalidNameException.class)
  ResponseEntity<ErrorResponseBody<MinLettersProperties>> handleInvalidName(
      InvalidNameException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new MinLettersProperties(Name.MIN_LETTERS)));
  }

  @ExceptionHandler(InvalidUserIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidUserId(
      InvalidUserIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler(InvalidVerificationTokenException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidVerificationToken(
      InvalidVerificationTokenException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new RequiredVersionProperties(VerificationToken.VERSION)));
  }

  @ExceptionHandler(RoleNotFoundException.class)
  ResponseEntity<ErrorResponseBody<RoleIdProperties>> handleRoleNotFound(
      RoleNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RoleIdProperties(exception.getId())));
  }

  @ExceptionHandler(InvalidRoleIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidRoleId(
      InvalidRoleIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(RoleId.VERSION)));
  }

  @ExceptionHandler({
    NullEmailException.class,
    NullNameException.class,
    NullPasswordException.class,
    NullRoleException.class,
    NullUserIdException.class,
    NullUsernameException.class,
    NullVerifiedException.class,
    PasswordMissingDigitException.class,
    PasswordMissingLowercaseException.class,
    PasswordMissingSpecialCharacterException.class,
    PasswordMissingUppercaseException.class,
    InvalidEmailException.class,
    InvalidUsernameException.class,
    VerificationTokenMismatchException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

package com.codeclash.problemservice.tag.adapter.inbound.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.TagIdProperties;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.TagNameProperties;
import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Name;
import com.codeclash.problemservice.tag.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.tag.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.tag.model.exception.DuplicateTagNameException;
import com.codeclash.problemservice.tag.model.exception.InvalidDescriptionException;
import com.codeclash.problemservice.tag.model.exception.InvalidNameException;
import com.codeclash.problemservice.tag.model.exception.InvalidTagIdException;
import com.codeclash.problemservice.tag.model.exception.NameTooLongException;
import com.codeclash.problemservice.tag.model.exception.NameTooShortException;
import com.codeclash.problemservice.tag.model.exception.NullDescriptionException;
import com.codeclash.problemservice.tag.model.exception.NullNameException;
import com.codeclash.problemservice.tag.model.exception.NullTagIdException;
import com.codeclash.problemservice.tag.model.exception.TagNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TagRestExceptionHandler {

  @ExceptionHandler(TagNotFoundException.class)
  ResponseEntity<ErrorResponseBody<TagIdProperties>> handleTagNotFound(
      TagNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new TagIdProperties(exception.getId())));
  }

  @ExceptionHandler(DuplicateTagNameException.class)
  ResponseEntity<ErrorResponseBody<TagNameProperties>> handleDuplicateTagName(
      DuplicateTagNameException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseBody.of(exception, new TagNameProperties(exception.getName())));
  }

  @ExceptionHandler({NameTooShortException.class, NameTooLongException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleNameLength(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RangeProperties(Name.MIN_SIZE, Name.MAX_SIZE)));
  }

  @ExceptionHandler({DescriptionTooShortException.class, DescriptionTooLongException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleDescriptionLength(
      CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new RangeProperties(Description.MIN_SIZE, Description.MAX_SIZE)));
  }

  @ExceptionHandler(InvalidTagIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidTagId(
      InvalidTagIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler({
    NullDescriptionException.class,
    NullTagIdException.class,
    NullNameException.class,
    InvalidDescriptionException.class,
    InvalidNameException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

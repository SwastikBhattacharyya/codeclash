package com.codeclash.problemservice.category.adapter.inbound.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.CategoryIdProperties;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.CategoryNameProperties;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import com.codeclash.problemservice.category.model.exception.CategoryNotFoundException;
import com.codeclash.problemservice.category.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.category.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.category.model.exception.DuplicateCategoryNameException;
import com.codeclash.problemservice.category.model.exception.InvalidCategoryIdException;
import com.codeclash.problemservice.category.model.exception.InvalidDescriptionException;
import com.codeclash.problemservice.category.model.exception.InvalidNameException;
import com.codeclash.problemservice.category.model.exception.NameTooLongException;
import com.codeclash.problemservice.category.model.exception.NameTooShortException;
import com.codeclash.problemservice.category.model.exception.NullCategoryIdException;
import com.codeclash.problemservice.category.model.exception.NullDescriptionException;
import com.codeclash.problemservice.category.model.exception.NullNameException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CategoryRestExceptionHandler {

  @ExceptionHandler(CategoryNotFoundException.class)
  ResponseEntity<ErrorResponseBody<CategoryIdProperties>> handleCategoryNotFound(
      CategoryNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new CategoryIdProperties(exception.getId())));
  }

  @ExceptionHandler(DuplicateCategoryNameException.class)
  ResponseEntity<ErrorResponseBody<CategoryNameProperties>> handleDuplicateCategoryName(
      DuplicateCategoryNameException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseBody.of(exception, new CategoryNameProperties(exception.getName())));
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

  @ExceptionHandler(InvalidCategoryIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidCategoryId(
      InvalidCategoryIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler({
    NullDescriptionException.class,
    NullCategoryIdException.class,
    NullNameException.class,
    InvalidDescriptionException.class,
    InvalidNameException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

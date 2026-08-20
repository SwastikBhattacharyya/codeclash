package com.codeclash.problemservice.question.adapter.inbound.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.CategoryIdsProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.QuestionIdProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.QuestionRunningContestProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.TagIdsProperties;
import com.codeclash.problemservice.question.api.exception.QuestionIdNotFoundException;
import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.category.CategoryId;
import com.codeclash.problemservice.question.model.exception.CategoriesNotFoundException;
import com.codeclash.problemservice.question.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.question.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.question.model.exception.InvalidCategoryIdException;
import com.codeclash.problemservice.question.model.exception.InvalidDescriptionException;
import com.codeclash.problemservice.question.model.exception.InvalidQuestionIdException;
import com.codeclash.problemservice.question.model.exception.InvalidSummaryException;
import com.codeclash.problemservice.question.model.exception.InvalidTagIdException;
import com.codeclash.problemservice.question.model.exception.InvalidTitleException;
import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooLowException;
import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooLowException;
import com.codeclash.problemservice.question.model.exception.NullCategoriesException;
import com.codeclash.problemservice.question.model.exception.NullDescriptionException;
import com.codeclash.problemservice.question.model.exception.NullMaxRamLimitException;
import com.codeclash.problemservice.question.model.exception.NullMaxRunTimeException;
import com.codeclash.problemservice.question.model.exception.NullQuestionIdException;
import com.codeclash.problemservice.question.model.exception.NullSummaryException;
import com.codeclash.problemservice.question.model.exception.NullTagsException;
import com.codeclash.problemservice.question.model.exception.NullTitleException;
import com.codeclash.problemservice.question.model.exception.NullVisibleException;
import com.codeclash.problemservice.question.model.exception.QuestionInRunningContestException;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.model.exception.SummaryTooLongException;
import com.codeclash.problemservice.question.model.exception.SummaryTooShortException;
import com.codeclash.problemservice.question.model.exception.TagsNotFoundException;
import com.codeclash.problemservice.question.model.exception.TitleTooLongException;
import com.codeclash.problemservice.question.model.exception.TitleTooShortException;
import com.codeclash.problemservice.question.model.tag.TagId;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class QuestionRestExceptionHandler {

  @ExceptionHandler(QuestionNotFoundException.class)
  ResponseEntity<ErrorResponseBody<QuestionIdProperties>> handleQuestionNotFound(
      QuestionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new QuestionIdProperties(exception.getId())));
  }

  @ExceptionHandler(QuestionInRunningContestException.class)
  ResponseEntity<ErrorResponseBody<QuestionRunningContestProperties>> handleQuestionInRunningContest(
      QuestionInRunningContestException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponseBody.of(
                exception,
                new QuestionRunningContestProperties(exception.getId(), exception.getContestIds())));
  }

  @ExceptionHandler(QuestionIdNotFoundException.class)
  ResponseEntity<ErrorResponseBody<QuestionIdProperties>> handleQuestionIdNotFound(
      QuestionIdNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new QuestionIdProperties(exception.getId())));
  }

  @ExceptionHandler(CategoriesNotFoundException.class)
  ResponseEntity<ErrorResponseBody<CategoryIdsProperties>> handleCategoriesNotFound(
      CategoriesNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new CategoryIdsProperties(exception.getIds())));
  }

  @ExceptionHandler(TagsNotFoundException.class)
  ResponseEntity<ErrorResponseBody<TagIdsProperties>> handleTagsNotFound(
      TagsNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new TagIdsProperties(exception.getIds())));
  }

  @ExceptionHandler({TitleTooShortException.class, TitleTooLongException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleTitleLength(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RangeProperties(Title.MIN_SIZE, Title.MAX_SIZE)));
  }

  @ExceptionHandler({SummaryTooShortException.class, SummaryTooLongException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleSummaryLength(
      CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new RangeProperties(Summary.MIN_SIZE, Summary.MAX_SIZE)));
  }

  @ExceptionHandler({DescriptionTooShortException.class, DescriptionTooLongException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleDescriptionLength(
      CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new RangeProperties(Description.MIN_SIZE, Description.MAX_SIZE)));
  }

  @ExceptionHandler({MaxRunTimeTooLowException.class, MaxRunTimeTooHighException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleMaxRunTime(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new RangeProperties(MaxRunTime.MIN, MaxRunTime.MAX)));
  }

  @ExceptionHandler({MaxRamLimitTooLowException.class, MaxRamLimitTooHighException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleMaxRamLimit(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new RangeProperties(MaxRamLimit.MIN, MaxRamLimit.MAX)));
  }

  @ExceptionHandler(InvalidQuestionIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidQuestionId(
      InvalidQuestionIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(Id.VERSION)));
  }

  @ExceptionHandler(InvalidCategoryIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidCategoryId(
      InvalidCategoryIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(CategoryId.VERSION)));
  }

  @ExceptionHandler(InvalidTagIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidTagId(
      InvalidTagIdException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RequiredVersionProperties(TagId.VERSION)));
  }

  @ExceptionHandler({
    NullCategoriesException.class,
    NullDescriptionException.class,
    NullMaxRamLimitException.class,
    NullMaxRunTimeException.class,
    NullQuestionIdException.class,
    NullSummaryException.class,
    NullTagsException.class,
    NullTitleException.class,
    NullVisibleException.class,
    InvalidDescriptionException.class,
    InvalidSummaryException.class,
    InvalidTitleException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

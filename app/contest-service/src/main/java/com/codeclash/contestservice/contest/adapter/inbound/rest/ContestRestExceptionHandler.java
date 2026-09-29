package com.codeclash.contestservice.contest.adapter.inbound.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestEndProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestIdProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestLiveProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestNameProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.QuestionIdsProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.RegistrationProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ScheduleProperties;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.UserIdProperties;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.exception.AlreadyRegisteredException;
import com.codeclash.contestservice.contest.model.exception.ContestAlreadyLiveException;
import com.codeclash.contestservice.contest.model.exception.ContestEndedException;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.exception.DescriptionTooLongException;
import com.codeclash.contestservice.contest.model.exception.DescriptionTooShortException;
import com.codeclash.contestservice.contest.model.exception.DuplicateContestNameException;
import com.codeclash.contestservice.contest.model.exception.DuplicateQuestionsException;
import com.codeclash.contestservice.contest.model.exception.EndAtInPastException;
import com.codeclash.contestservice.contest.model.exception.EndAtNotAfterLiveAtException;
import com.codeclash.contestservice.contest.model.exception.InvalidContestIdException;
import com.codeclash.contestservice.contest.model.exception.InvalidDescriptionException;
import com.codeclash.contestservice.contest.model.exception.InvalidNameException;
import com.codeclash.contestservice.contest.model.exception.InvalidQuestionIdException;
import com.codeclash.contestservice.contest.model.exception.LiveAtInPastException;
import com.codeclash.contestservice.contest.model.exception.NameTooLongException;
import com.codeclash.contestservice.contest.model.exception.NameTooShortException;
import com.codeclash.contestservice.contest.model.exception.NullContestIdException;
import com.codeclash.contestservice.contest.model.exception.NullDescriptionException;
import com.codeclash.contestservice.contest.model.exception.NullEndAtException;
import com.codeclash.contestservice.contest.model.exception.NullLiveAtException;
import com.codeclash.contestservice.contest.model.exception.NullNameException;
import com.codeclash.contestservice.contest.model.exception.NullPointsException;
import com.codeclash.contestservice.contest.model.exception.NullQuestionIdException;
import com.codeclash.contestservice.contest.model.exception.NullQuestionsException;
import com.codeclash.contestservice.contest.model.exception.PointsTooHighException;
import com.codeclash.contestservice.contest.model.exception.PointsTooLowException;
import com.codeclash.contestservice.contest.model.exception.QuestionsNotFoundException;
import com.codeclash.contestservice.contest.model.exception.RegistrationNotFoundException;
import com.codeclash.contestservice.contest.model.exception.RegistrationUserNotFoundException;
import com.codeclash.contestservice.contest.model.question.Points;
import com.codeclash.contestservice.contest.model.question.QuestionId;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContestRestExceptionHandler {

  @ExceptionHandler(ContestNotFoundException.class)
  ResponseEntity<ErrorResponseBody<ContestIdProperties>> handleContestNotFound(
      ContestNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new ContestIdProperties(exception.getId())));
  }

  @ExceptionHandler(DuplicateContestNameException.class)
  ResponseEntity<ErrorResponseBody<ContestNameProperties>> handleDuplicateContestName(
      DuplicateContestNameException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponseBody.of(exception, new ContestNameProperties(exception.getName())));
  }

  @ExceptionHandler(RegistrationNotFoundException.class)
  ResponseEntity<ErrorResponseBody<RegistrationProperties>> handleRegistrationNotFound(
      RegistrationNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ErrorResponseBody.of(
                exception,
                new RegistrationProperties(exception.getContestId(), exception.getUserId())));
  }

  @ExceptionHandler(RegistrationUserNotFoundException.class)
  ResponseEntity<ErrorResponseBody<UserIdProperties>> handleRegistrationUserNotFound(
      RegistrationUserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseBody.of(exception, new UserIdProperties(exception.getUserId())));
  }

  @ExceptionHandler(AlreadyRegisteredException.class)
  ResponseEntity<ErrorResponseBody<RegistrationProperties>> handleAlreadyRegistered(
      AlreadyRegisteredException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponseBody.of(
                exception,
                new RegistrationProperties(exception.getContestId(), exception.getUserId())));
  }

  @ExceptionHandler(ContestEndedException.class)
  ResponseEntity<ErrorResponseBody<ContestEndProperties>> handleContestEnded(
      ContestEndedException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponseBody.of(
                exception, new ContestEndProperties(exception.getId(), exception.getEndAt())));
  }

  @ExceptionHandler(ContestAlreadyLiveException.class)
  ResponseEntity<ErrorResponseBody<ContestLiveProperties>> handleContestAlreadyLive(
      ContestAlreadyLiveException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponseBody.of(
                exception, new ContestLiveProperties(exception.getId(), exception.getLiveAt())));
  }

  @ExceptionHandler(QuestionsNotFoundException.class)
  ResponseEntity<ErrorResponseBody<QuestionIdsProperties>> handleQuestionsNotFound(
      QuestionsNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new QuestionIdsProperties(exception.getIds())));
  }

  @ExceptionHandler(DuplicateQuestionsException.class)
  ResponseEntity<ErrorResponseBody<QuestionIdsProperties>> handleDuplicateQuestions(
      DuplicateQuestionsException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new QuestionIdsProperties(exception.getIds())));
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

  @ExceptionHandler({PointsTooLowException.class, PointsTooHighException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handlePoints(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RangeProperties(Points.MIN, Points.MAX)));
  }

  @ExceptionHandler(EndAtNotAfterLiveAtException.class)
  ResponseEntity<ErrorResponseBody<ScheduleProperties>> handleEndAtNotAfterLiveAt(
      EndAtNotAfterLiveAtException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception, new ScheduleProperties(exception.getLiveAt(), exception.getEndAt())));
  }

  @ExceptionHandler(InvalidContestIdException.class)
  ResponseEntity<ErrorResponseBody<RequiredVersionProperties>> handleInvalidContestId(
      InvalidContestIdException exception) {
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
    NullContestIdException.class,
    NullDescriptionException.class,
    NullEndAtException.class,
    NullLiveAtException.class,
    NullNameException.class,
    NullPointsException.class,
    NullQuestionIdException.class,
    NullQuestionsException.class,
    InvalidDescriptionException.class,
    InvalidNameException.class,
    LiveAtInPastException.class,
    EndAtInPastException.class
  })
  ResponseEntity<ErrorResponseBody<Void>> handleValidation(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception));
  }
}

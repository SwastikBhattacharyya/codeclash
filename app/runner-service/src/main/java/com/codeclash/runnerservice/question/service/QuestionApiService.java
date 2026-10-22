package com.codeclash.runnerservice.question.service;

import com.codeclash.runnerservice.question.api.QuestionLimitsView;
import com.codeclash.runnerservice.question.api.exception.QuestionIdNotFoundException;
import com.codeclash.runnerservice.question.api.usecase.FindQuestionLimitsApiUseCase;
import com.codeclash.runnerservice.question.model.Id;
import com.codeclash.runnerservice.question.model.Question;
import com.codeclash.runnerservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.runnerservice.question.port.inbound.GetQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QuestionApiService implements FindQuestionLimitsApiUseCase {

  private final GetQuestionUseCase getQuestionUseCase;

  private static QuestionLimitsView toView(Question question) {
    return new QuestionLimitsView(
        question.id().value(), question.maxRunTime().value(), question.maxRamLimit().value());
  }

  @Override
  public QuestionLimitsView findById(UUID id) {
    try {
      return QuestionApiService.toView(this.getQuestionUseCase.findById(Id.unsafe(id)));
    } catch (QuestionNotFoundException exception) {
      throw new QuestionIdNotFoundException(id);
    }
  }
}

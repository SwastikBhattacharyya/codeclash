package com.codeclash.problemservice.question.service;

import com.codeclash.problemservice.question.api.QuestionView;
import com.codeclash.problemservice.question.api.exception.QuestionIdNotFoundException;
import com.codeclash.problemservice.question.api.usecase.FindQuestionByIdApiUseCase;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.port.inbound.GetQuestionUseCase;
import io.micrometer.observation.annotation.Observed;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QuestionApiService implements FindQuestionByIdApiUseCase {

  private final GetQuestionUseCase getQuestionUseCase;

  @Override
  public QuestionView findById(UUID id) {
    try {
      return QuestionApiService.toView(this.getQuestionUseCase.findById(Id.unsafe(id)));
    } catch (QuestionNotFoundException exception) {
      throw new QuestionIdNotFoundException(id);
    }
  }

  private static QuestionView toView(Question question) {
    return new QuestionView(
        question.id().value(),
        question.visible().value(),
        question.createdAt().value(),
        question.updatedAt().value());
  }
}

package com.codeclash.problemservice.testcase.adapter.outbound.question;

import com.codeclash.problemservice.question.api.QuestionView;
import com.codeclash.problemservice.question.api.exception.QuestionIdNotFoundException;
import com.codeclash.problemservice.question.api.usecase.FindQuestionByIdApiUseCase;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.testcase.port.outbound.QuestionLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionApiAdapter implements QuestionLookup {

  private final FindQuestionByIdApiUseCase findQuestionByIdApiUseCase;

  @Override
  public QuestionView findById(QuestionId questionId) {
    try {
      return this.findQuestionByIdApiUseCase.findById(questionId.value());
    } catch (QuestionIdNotFoundException exception) {
      throw new QuestionNotFoundException(questionId.value());
    }
  }
}

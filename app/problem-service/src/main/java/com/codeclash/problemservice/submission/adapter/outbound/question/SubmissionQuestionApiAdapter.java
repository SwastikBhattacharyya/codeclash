package com.codeclash.problemservice.submission.adapter.outbound.question;

import com.codeclash.problemservice.question.api.QuestionView;
import com.codeclash.problemservice.question.api.exception.QuestionIdNotFoundException;
import com.codeclash.problemservice.question.api.usecase.FindQuestionByIdApiUseCase;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.submission.port.outbound.QuestionLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionQuestionApiAdapter implements QuestionLookup {

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

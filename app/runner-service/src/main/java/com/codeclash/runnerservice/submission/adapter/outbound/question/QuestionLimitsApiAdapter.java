package com.codeclash.runnerservice.submission.adapter.outbound.question;

import com.codeclash.runnerservice.submission.model.JudgeLimits;
import com.codeclash.runnerservice.submission.model.QuestionId;
import com.codeclash.runnerservice.submission.port.outbound.QuestionLimitsLookup;
import com.codeclash.runnerservice.question.api.QuestionLimitsView;
import com.codeclash.runnerservice.question.api.exception.QuestionIdNotFoundException;
import com.codeclash.runnerservice.question.api.usecase.FindQuestionLimitsApiUseCase;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionLimitsApiAdapter implements QuestionLimitsLookup {

  private final FindQuestionLimitsApiUseCase findQuestionLimitsApiUseCase;

  @Override
  public Optional<JudgeLimits> find(QuestionId questionId) {
    try {
      QuestionLimitsView view = this.findQuestionLimitsApiUseCase.findById(questionId.value());
      return Optional.of(new JudgeLimits(view.maxRunTimeMs(), view.maxRamLimitMb()));
    } catch (QuestionIdNotFoundException exception) {
      return Optional.empty();
    }
  }
}

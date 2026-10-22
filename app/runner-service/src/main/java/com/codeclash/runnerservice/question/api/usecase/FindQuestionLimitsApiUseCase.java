package com.codeclash.runnerservice.question.api.usecase;

import com.codeclash.runnerservice.question.api.QuestionLimitsView;
import java.util.UUID;

public interface FindQuestionLimitsApiUseCase {

  QuestionLimitsView findById(UUID id);
}

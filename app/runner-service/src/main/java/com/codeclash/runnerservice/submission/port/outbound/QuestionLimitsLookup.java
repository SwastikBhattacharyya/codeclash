package com.codeclash.runnerservice.submission.port.outbound;

import com.codeclash.runnerservice.submission.model.JudgeLimits;
import com.codeclash.runnerservice.submission.model.QuestionId;
import java.util.Optional;

public interface QuestionLimitsLookup {

  Optional<JudgeLimits> find(QuestionId questionId);
}

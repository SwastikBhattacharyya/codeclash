package com.codeclash.problemservice.submission.port.outbound;

import com.codeclash.problemservice.question.api.QuestionView;
import com.codeclash.problemservice.submission.model.QuestionId;

public interface QuestionLookup {

  QuestionView findById(QuestionId questionId);
}

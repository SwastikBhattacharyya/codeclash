package com.codeclash.problemservice.testcase.port.outbound;

import com.codeclash.problemservice.question.api.QuestionView;
import com.codeclash.problemservice.testcase.model.QuestionId;

public interface QuestionLookup {

  QuestionView findById(QuestionId questionId);
}

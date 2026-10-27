package com.codeclash.runnerservice.submission.port.outbound;

import com.codeclash.runnerservice.submission.model.JudgeTestCase;
import com.codeclash.runnerservice.submission.model.QuestionId;
import java.util.List;

public interface TestCaseLookup {

  List<JudgeTestCase> findByQuestionId(QuestionId questionId);
}

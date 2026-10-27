package com.codeclash.runnerservice.submission.model.request;

import com.codeclash.runnerservice.submission.model.Code;
import com.codeclash.runnerservice.submission.model.Id;
import com.codeclash.runnerservice.submission.model.QuestionId;
import com.codeclash.runnerservice.submission.model.exception.NullLanguageException;
import com.codeclash.contracts.problem.submission.Language;

public record EnqueueSubmissionRequest(Id id, Language language, Code code, QuestionId questionId) {

  public EnqueueSubmissionRequest {
    if (language == null) throw new NullLanguageException();
  }
}

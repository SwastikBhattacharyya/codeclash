package com.codeclash.problemservice.submission.model.request;

import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.problemservice.submission.model.Code;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.UserId;
import com.codeclash.problemservice.submission.model.owner.Owned;
import com.codeclash.problemservice.submission.model.owner.OwnerState;

public record CreateSubmissionRequest<O extends OwnerState>(
    QuestionId questionId, Language language, Code code, O owner) {

  public CreateSubmissionRequest<Owned> withOwner(UserId userId) {
    return new CreateSubmissionRequest<>(
        this.questionId, this.language, this.code, new Owned(userId));
  }
}

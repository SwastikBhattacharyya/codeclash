package com.codeclash.problemservice.submission.service;

import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.port.outbound.QuestionLookup;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SubmissionQuestionGuard {

  private final CurrentSession currentSession;
  private final QuestionLookup questionLookup;

  boolean canReadPrivate() {
    return this.currentSession.hasPermission(ProblemPermissions.QUESTION_READ_IS_PRIVATE);
  }

  void requireQuestionReadable(QuestionId questionId) {
    if (!this.canReadPrivate() && !this.questionLookup.findById(questionId).visible())
      throw new ForbiddenException(List.of(ProblemPermissions.QUESTION_READ_IS_PRIVATE));
  }
}

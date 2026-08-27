package com.codeclash.problemservice.submission.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.UserId;
import com.codeclash.problemservice.submission.model.owner.Unowned;
import com.codeclash.problemservice.submission.model.request.CreateSubmissionRequest;
import com.codeclash.problemservice.submission.port.inbound.CreateSubmissionSecuredUseCase;
import com.codeclash.problemservice.submission.port.inbound.CreateSubmissionUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateSubmissionSecuredService implements CreateSubmissionSecuredUseCase {

  private final CreateSubmissionUseCase createSubmissionUseCase;
  private final SubmissionQuestionGuard submissionQuestionGuard;
  private final CurrentSession currentSession;

  @Override
  @Authorized(ProblemPermissions.SUBMISSION_WRITE)
  public Submission create(CreateSubmissionRequest<Unowned> request) {
    this.submissionQuestionGuard.requireQuestionReadable(request.questionId());
    return this.createSubmissionUseCase.create(
        request.withOwner(UserId.unsafe(this.currentSession.userId())));
  }
}

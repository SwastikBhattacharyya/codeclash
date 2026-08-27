package com.codeclash.problemservice.submission.service;

import com.codeclash.contracts.auth.session.Authenticated;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.port.inbound.DeleteSubmissionSecuredUseCase;
import com.codeclash.problemservice.submission.port.inbound.DeleteSubmissionUseCase;
import com.codeclash.problemservice.submission.port.inbound.GetSubmissionUseCase;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteSubmissionSecuredService implements DeleteSubmissionSecuredUseCase {

  private final DeleteSubmissionUseCase deleteSubmissionUseCase;
  private final GetSubmissionUseCase getSubmissionUseCase;
  private final CurrentSession currentSession;

  @Override
  @Authenticated
  public void delete(Id id) {
    boolean other = this.currentSession.hasPermission(ProblemPermissions.SUBMISSION_DELETE);
    boolean own = this.currentSession.hasPermission(ProblemPermissions.SUBMISSION_DELETE_OWN);
    if (!other && !own)
      throw new ForbiddenException(
          List.of(ProblemPermissions.SUBMISSION_DELETE, ProblemPermissions.SUBMISSION_DELETE_OWN));
    if (!other || !own) {
      String permission =
          this.currentSession
                  .userId()
                  .equals(this.getSubmissionUseCase.findById(id).userId().value())
              ? ProblemPermissions.SUBMISSION_DELETE_OWN
              : ProblemPermissions.SUBMISSION_DELETE;
      if (!this.currentSession.hasPermission(permission))
        throw new ForbiddenException(List.of(permission));
    }
    this.deleteSubmissionUseCase.delete(id);
  }
}

package com.codeclash.problemservice.tag.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.port.inbound.DeleteTagSecuredUseCase;
import com.codeclash.problemservice.tag.port.inbound.DeleteTagUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteTagSecuredService implements DeleteTagSecuredUseCase {

  private final DeleteTagUseCase deleteTagUseCase;

  @Override
  @Authorized(ProblemPermissions.TAG_WRITE)
  public void delete(Id id) {
    this.deleteTagUseCase.delete(id);
  }
}

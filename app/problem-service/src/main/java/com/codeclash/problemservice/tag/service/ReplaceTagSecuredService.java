package com.codeclash.problemservice.tag.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.tag.model.request.ReplaceTagRequest;
import com.codeclash.problemservice.tag.model.result.ReplaceTagResult;
import com.codeclash.problemservice.tag.port.inbound.ReplaceTagSecuredUseCase;
import com.codeclash.problemservice.tag.port.inbound.ReplaceTagUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ReplaceTagSecuredService implements ReplaceTagSecuredUseCase {

  private final ReplaceTagUseCase replaceTagUseCase;

  @Override
  @Authorized(ProblemPermissions.TAG_WRITE)
  public ReplaceTagResult replace(ReplaceTagRequest request) {
    return this.replaceTagUseCase.replace(request);
  }
}

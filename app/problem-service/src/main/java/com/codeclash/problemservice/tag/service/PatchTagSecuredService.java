package com.codeclash.problemservice.tag.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.PatchTagRequest;
import com.codeclash.problemservice.tag.port.inbound.PatchTagSecuredUseCase;
import com.codeclash.problemservice.tag.port.inbound.PatchTagUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PatchTagSecuredService implements PatchTagSecuredUseCase {

  private final PatchTagUseCase patchTagUseCase;

  @Override
  @Authorized(ProblemPermissions.TAG_WRITE)
  public Tag patch(PatchTagRequest request) {
    return this.patchTagUseCase.patch(request);
  }
}

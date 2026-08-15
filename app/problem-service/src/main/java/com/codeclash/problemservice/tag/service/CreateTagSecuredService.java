package com.codeclash.problemservice.tag.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.CreateTagRequest;
import com.codeclash.problemservice.tag.port.inbound.CreateTagSecuredUseCase;
import com.codeclash.problemservice.tag.port.inbound.CreateTagUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateTagSecuredService implements CreateTagSecuredUseCase {

  private final CreateTagUseCase createTagUseCase;

  @Override
  @Authorized(ProblemPermissions.TAG_WRITE)
  public Tag create(CreateTagRequest request) {
    return this.createTagUseCase.create(request);
  }
}

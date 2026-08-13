package com.codeclash.problemservice.category.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.category.model.request.ReplaceCategoryRequest;
import com.codeclash.problemservice.category.model.result.ReplaceCategoryResult;
import com.codeclash.problemservice.category.port.inbound.ReplaceCategorySecuredUseCase;
import com.codeclash.problemservice.category.port.inbound.ReplaceCategoryUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ReplaceCategorySecuredService implements ReplaceCategorySecuredUseCase {

  private final ReplaceCategoryUseCase replaceCategoryUseCase;

  @Override
  @Authorized(ProblemPermissions.CATEGORY_WRITE)
  public ReplaceCategoryResult replace(ReplaceCategoryRequest request) {
    return this.replaceCategoryUseCase.replace(request);
  }
}

package com.codeclash.problemservice.category.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.request.PatchCategoryRequest;
import com.codeclash.problemservice.category.port.inbound.PatchCategorySecuredUseCase;
import com.codeclash.problemservice.category.port.inbound.PatchCategoryUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PatchCategorySecuredService implements PatchCategorySecuredUseCase {

  private final PatchCategoryUseCase patchCategoryUseCase;

  @Override
  @Authorized(ProblemPermissions.CATEGORY_WRITE)
  public Category patch(PatchCategoryRequest request) {
    return this.patchCategoryUseCase.patch(request);
  }
}

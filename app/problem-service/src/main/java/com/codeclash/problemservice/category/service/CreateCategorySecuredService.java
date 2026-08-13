package com.codeclash.problemservice.category.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.request.CreateCategoryRequest;
import com.codeclash.problemservice.category.port.inbound.CreateCategorySecuredUseCase;
import com.codeclash.problemservice.category.port.inbound.CreateCategoryUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateCategorySecuredService implements CreateCategorySecuredUseCase {

  private final CreateCategoryUseCase createCategoryUseCase;

  @Override
  @Authorized(ProblemPermissions.CATEGORY_WRITE)
  public Category create(CreateCategoryRequest request) {
    return this.createCategoryUseCase.create(request);
  }
}

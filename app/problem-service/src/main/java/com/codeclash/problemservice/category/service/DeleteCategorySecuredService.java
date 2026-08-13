package com.codeclash.problemservice.category.service;

import com.codeclash.contracts.auth.session.Authorized;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.port.inbound.DeleteCategorySecuredUseCase;
import com.codeclash.problemservice.category.port.inbound.DeleteCategoryUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteCategorySecuredService implements DeleteCategorySecuredUseCase {

  private final DeleteCategoryUseCase deleteCategoryUseCase;

  @Override
  @Authorized(ProblemPermissions.CATEGORY_WRITE)
  public void delete(Id id) {
    this.deleteCategoryUseCase.delete(id);
  }
}

package com.codeclash.problemservice.category.service;

import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.exception.CategoryNotFoundException;
import com.codeclash.problemservice.category.port.inbound.GetCategoryUseCase;
import com.codeclash.problemservice.category.port.outbound.CategoryPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetCategoryService implements GetCategoryUseCase {

  private final CategoryPersistence categoryPersistence;

  @Override
  public Category findById(Id id) {
    return this.categoryPersistence
        .findById(id)
        .orElseThrow(() -> new CategoryNotFoundException(id.value()));
  }
}

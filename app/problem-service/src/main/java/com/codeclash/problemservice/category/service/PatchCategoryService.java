package com.codeclash.problemservice.category.service;

import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.exception.CategoryNotFoundException;
import com.codeclash.problemservice.category.model.request.PatchCategoryRequest;
import com.codeclash.problemservice.category.port.inbound.PatchCategoryUseCase;
import com.codeclash.problemservice.category.port.outbound.CategoryPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class PatchCategoryService implements PatchCategoryUseCase {

  private final CategoryPersistence categoryPersistence;

  @Override
  public Category patch(PatchCategoryRequest request) {
    Category category =
        this.categoryPersistence
            .patch(request)
            .orElseThrow(() -> new CategoryNotFoundException(request.id().value()));
    PatchCategoryService.log
        .atInfo()
        .setMessage("category patched")
        .addKeyValue("category.id", category.id().value())
        .log();
    return category;
  }
}

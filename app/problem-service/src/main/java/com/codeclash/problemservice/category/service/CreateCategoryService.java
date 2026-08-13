package com.codeclash.problemservice.category.service;

import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.request.CreateCategoryRequest;
import com.codeclash.problemservice.category.port.inbound.CreateCategoryUseCase;
import com.codeclash.problemservice.category.port.outbound.CategoryPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateCategoryService implements CreateCategoryUseCase {

  private final CategoryPersistence categoryPersistence;

  @Override
  public Category create(CreateCategoryRequest request) {
    Category category = this.categoryPersistence.create(request);
    CreateCategoryService.log
        .atInfo()
        .setMessage("category created")
        .addKeyValue("category.id", category.id().value())
        .log();
    return category;
  }
}

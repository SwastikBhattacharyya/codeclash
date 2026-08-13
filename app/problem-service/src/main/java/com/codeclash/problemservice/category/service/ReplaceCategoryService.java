package com.codeclash.problemservice.category.service;

import com.codeclash.problemservice.category.model.request.ReplaceCategoryRequest;
import com.codeclash.problemservice.category.model.result.ReplaceCategoryResult;
import com.codeclash.problemservice.category.port.inbound.ReplaceCategoryUseCase;
import com.codeclash.problemservice.category.port.outbound.CategoryPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class ReplaceCategoryService implements ReplaceCategoryUseCase {

  private final CategoryPersistence categoryPersistence;

  @Override
  public ReplaceCategoryResult replace(ReplaceCategoryRequest request) {
    ReplaceCategoryResult result = this.categoryPersistence.replace(request);
    ReplaceCategoryService.log
        .atInfo()
        .setMessage("category replaced")
        .addKeyValue("category.id", result.category().id().value())
        .addKeyValue("created", result.created())
        .log();
    return result;
  }
}

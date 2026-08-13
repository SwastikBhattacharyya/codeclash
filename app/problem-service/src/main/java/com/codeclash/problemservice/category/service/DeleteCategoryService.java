package com.codeclash.problemservice.category.service;

import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.exception.CategoryNotFoundException;
import com.codeclash.problemservice.category.port.inbound.DeleteCategoryUseCase;
import com.codeclash.problemservice.category.port.outbound.CategoryPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class DeleteCategoryService implements DeleteCategoryUseCase {

  private final CategoryPersistence categoryPersistence;

  @Override
  public void delete(Id id) {
    if (!this.categoryPersistence.deleteById(id)) throw new CategoryNotFoundException(id.value());
    DeleteCategoryService.log
        .atInfo()
        .setMessage("category deleted")
        .addKeyValue("category.id", id.value())
        .log();
  }
}

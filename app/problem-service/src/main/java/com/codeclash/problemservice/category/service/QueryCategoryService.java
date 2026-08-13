package com.codeclash.problemservice.category.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.request.QueryCategoryRequest;
import com.codeclash.problemservice.category.port.inbound.QueryCategoryUseCase;
import com.codeclash.problemservice.category.port.outbound.CategoryPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryCategoryService implements QueryCategoryUseCase {

  private final CategoryPersistence categoryPersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<Category> query(QueryCategoryRequest request) {
    return this.categoryPersistence.query(request);
  }
}

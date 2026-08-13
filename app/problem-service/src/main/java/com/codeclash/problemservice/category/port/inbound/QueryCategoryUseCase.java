package com.codeclash.problemservice.category.port.inbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.request.QueryCategoryRequest;

public interface QueryCategoryUseCase {

  PagedEntity<Category> query(QueryCategoryRequest request);
}

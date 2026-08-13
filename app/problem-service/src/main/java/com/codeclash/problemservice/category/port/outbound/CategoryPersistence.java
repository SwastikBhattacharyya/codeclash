package com.codeclash.problemservice.category.port.outbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.request.CreateCategoryRequest;
import com.codeclash.problemservice.category.model.request.PatchCategoryRequest;
import com.codeclash.problemservice.category.model.request.QueryCategoryRequest;
import com.codeclash.problemservice.category.model.request.ReplaceCategoryRequest;
import com.codeclash.problemservice.category.model.result.ReplaceCategoryResult;
import java.util.Optional;

public interface CategoryPersistence {

  PagedEntity<Category> query(QueryCategoryRequest request);

  Optional<Category> findById(Id id);

  Category create(CreateCategoryRequest request);

  ReplaceCategoryResult replace(ReplaceCategoryRequest request);

  Optional<Category> patch(PatchCategoryRequest request);

  boolean deleteById(Id id);
}

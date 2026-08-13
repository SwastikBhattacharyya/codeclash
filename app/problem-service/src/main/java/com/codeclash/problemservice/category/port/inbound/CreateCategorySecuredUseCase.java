package com.codeclash.problemservice.category.port.inbound;

import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.request.CreateCategoryRequest;

public interface CreateCategorySecuredUseCase {

  Category create(CreateCategoryRequest request);
}

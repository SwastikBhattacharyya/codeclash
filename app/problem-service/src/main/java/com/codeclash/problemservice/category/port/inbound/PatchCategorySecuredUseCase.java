package com.codeclash.problemservice.category.port.inbound;

import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.request.PatchCategoryRequest;

public interface PatchCategorySecuredUseCase {

  Category patch(PatchCategoryRequest request);
}

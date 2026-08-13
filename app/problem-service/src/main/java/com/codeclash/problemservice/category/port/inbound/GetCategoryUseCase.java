package com.codeclash.problemservice.category.port.inbound;

import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.Id;

public interface GetCategoryUseCase {

  Category findById(Id id);
}

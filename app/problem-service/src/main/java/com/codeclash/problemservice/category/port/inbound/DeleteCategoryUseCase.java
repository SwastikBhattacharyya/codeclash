package com.codeclash.problemservice.category.port.inbound;

import com.codeclash.problemservice.category.model.Id;

public interface DeleteCategoryUseCase {

  void delete(Id id);
}

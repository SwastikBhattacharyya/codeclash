package com.codeclash.problemservice.category.port.inbound;

import com.codeclash.problemservice.category.model.Id;

public interface DeleteCategorySecuredUseCase {

  void delete(Id id);
}

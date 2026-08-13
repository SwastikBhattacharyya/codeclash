package com.codeclash.problemservice.category.port.inbound;

import com.codeclash.problemservice.category.model.request.ReplaceCategoryRequest;
import com.codeclash.problemservice.category.model.result.ReplaceCategoryResult;

public interface ReplaceCategoryUseCase {

  ReplaceCategoryResult replace(ReplaceCategoryRequest request);
}

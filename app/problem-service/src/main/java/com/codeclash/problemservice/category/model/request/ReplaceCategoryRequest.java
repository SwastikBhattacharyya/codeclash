package com.codeclash.problemservice.category.model.request;

import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import lombok.Builder;

@Builder
public record ReplaceCategoryRequest(Id id, Name name, Description description) {}

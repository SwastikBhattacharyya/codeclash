package com.codeclash.problemservice.category.model.request;

import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Name;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record CreateCategoryRequest(Name name, Description description) {}

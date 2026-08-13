package com.codeclash.problemservice.category.model.result;

import com.codeclash.problemservice.category.model.Category;

public record ReplaceCategoryResult(Category category, boolean created) {}

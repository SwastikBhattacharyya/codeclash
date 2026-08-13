package com.codeclash.problemservice.category.model.request;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import lombok.Builder;

@Builder
public record PatchCategoryRequest(
    Id id, PatchObject<Name> name, PatchObject<Description> description) {}

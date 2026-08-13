package com.codeclash.problemservice.category.adapter.inbound.rest.dto;

import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import com.codeclash.problemservice.category.model.request.ReplaceCategoryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Full replacement of a category; creates it when the id does not exist")
public record ReplaceCategoryRequestBody(
    @Schema(
            description = "Category name on a single line; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "graph-theory")
        String name,
    @Schema(
            description = "Category description; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Problems on graphs and their traversal")
        String description) {

  public ReplaceCategoryRequest toRequest(UUID id) {
    return new ReplaceCategoryRequest(
        Id.of(id), Name.of(this.name), Description.of(this.description));
  }
}

package com.codeclash.problemservice.tag.adapter.inbound.rest.dto;

import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Name;
import com.codeclash.problemservice.tag.model.request.CreateTagRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tag to create")
public record CreateTagRequestBody(
    @Schema(
            description = "Tag name on a single line; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "dynamic-programming")
        String name,
    @Schema(
            description = "Tag description; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Problems solved with dynamic programming")
        String description) {

  public CreateTagRequest toRequest() {
    return new CreateTagRequest(Name.of(this.name), Description.of(this.description));
  }
}

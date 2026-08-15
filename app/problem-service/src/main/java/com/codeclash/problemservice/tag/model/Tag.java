package com.codeclash.problemservice.tag.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record Tag(
    Id id, Name name, Description description, CreatedAt createdAt, UpdatedAt updatedAt) {}

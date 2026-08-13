package com.codeclash.problemservice.category.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record Category(
    Id id, Name name, Description description, CreatedAt createdAt, UpdatedAt updatedAt) {}

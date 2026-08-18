package com.codeclash.problemservice.question.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.problemservice.question.model.category.Categories;
import com.codeclash.problemservice.question.model.tag.Tags;
import lombok.With;

@With
public record Question(
    Id id,
    Title title,
    Summary summary,
    Description description,
    Visible visible,
    MaxRunTime maxRunTime,
    MaxRamLimit maxRamLimit,
    Categories categories,
    Tags tags,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {}

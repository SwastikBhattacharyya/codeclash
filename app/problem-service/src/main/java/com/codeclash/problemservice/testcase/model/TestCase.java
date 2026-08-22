package com.codeclash.problemservice.testcase.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;

public record TestCase(
    Id id,
    Input input,
    Output output,
    Public isPublic,
    QuestionId questionId,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {}

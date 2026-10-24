package com.codeclash.runnerservice.testcase.model;

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

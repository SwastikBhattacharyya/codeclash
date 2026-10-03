package com.codeclash.contestservice.submission.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contracts.problem.submission.SubmissionState;
import java.util.UUID;

public record Submission(
    Id id,
    UUID questionId,
    UUID userId,
    SubmissionState state,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {}

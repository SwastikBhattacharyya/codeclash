package com.codeclash.contestservice.contest.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;
import lombok.With;

@With
public record Contest(
    Id id,
    Name name,
    Description description,
    LiveAt liveAt,
    EndAt endAt,
    OpenedAt openedAt,
    ContestQuestions questions,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {}

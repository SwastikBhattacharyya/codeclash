package com.codeclash.runnerservice.question.model;

import com.codeclash.common.timestamp.UpdatedAt;

public record Question(Id id, MaxRunTime maxRunTime, MaxRamLimit maxRamLimit, UpdatedAt updatedAt) {}

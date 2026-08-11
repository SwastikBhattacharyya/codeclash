package com.codeclash.problemservice.user.model;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;

public record User(Id id, Username username, CreatedAt createdAt, UpdatedAt updatedAt) {}

package com.codeclash.problemservice.submission.model.owner;

import com.codeclash.problemservice.submission.model.UserId;

public record Owned(UserId userId) implements OwnerState {}

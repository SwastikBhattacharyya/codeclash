package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AlreadyRegisteredException extends CodedException {

  private final UUID contestId;
  private final UUID userId;

  public AlreadyRegisteredException(UUID contestId, UUID userId) {
    super(
        ContestErrorCode.ALREADY_REGISTERED,
        "User %s is already registered for contest %s".formatted(userId, contestId));
    this.contestId = contestId;
    this.userId = userId;
  }
}

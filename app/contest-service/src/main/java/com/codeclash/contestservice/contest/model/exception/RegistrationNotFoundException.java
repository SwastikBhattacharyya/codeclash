package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RegistrationNotFoundException extends CodedException {

  private final UUID contestId;
  private final UUID userId;

  public RegistrationNotFoundException(UUID contestId, UUID userId) {
    super(
        ContestErrorCode.REGISTRATION_NOT_FOUND,
        "User %s is not registered for contest %s".formatted(userId, contestId));
    this.contestId = contestId;
    this.userId = userId;
  }
}

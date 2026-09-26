package com.codeclash.contestservice.contest.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RegistrationUserNotFoundException extends CodedException {

  private final UUID userId;

  public RegistrationUserNotFoundException(UUID userId) {
    super(
        ContestErrorCode.REGISTRATION_USER_NOT_FOUND,
        "User with id %s not found".formatted(userId));
    this.userId = userId;
  }
}

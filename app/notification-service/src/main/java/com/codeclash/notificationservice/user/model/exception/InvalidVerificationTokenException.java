package com.codeclash.notificationservice.user.model.exception;

import com.codeclash.common.exception.CodedException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InvalidVerificationTokenException extends CodedException {

  private final UUID token;

  public InvalidVerificationTokenException(UUID token) {
    super(
        UserErrorCode.INVALID_VERIFICATION_TOKEN,
        "%s is not a valid verification token".formatted(token));
    this.token = token;
  }
}

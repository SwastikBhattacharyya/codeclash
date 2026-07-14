package com.codeclash.authservice.user.api.usecase;

import java.util.UUID;

public interface IsUserVerifiedApiUseCase {

  boolean isVerified(UUID userId);
}

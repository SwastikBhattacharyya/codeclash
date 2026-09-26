package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.registration.UserId;

public interface GetRegistrationUseCase {

  boolean isRegistered(Id id, UserId userId);
}

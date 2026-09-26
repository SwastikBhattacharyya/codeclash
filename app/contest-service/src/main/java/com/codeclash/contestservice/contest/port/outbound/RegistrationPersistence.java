package com.codeclash.contestservice.contest.port.outbound;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.registration.UserId;

public interface RegistrationPersistence {

  void insert(Id id, UserId userId);

  boolean delete(Id id, UserId userId);

  boolean exists(Id id, UserId userId);
}

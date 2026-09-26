package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.inbound.GetRegistrationUseCase;
import com.codeclash.contestservice.contest.port.outbound.RegistrationPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetRegistrationService implements GetRegistrationUseCase {

  private final RegistrationPersistence registrationPersistence;

  @Override
  public boolean isRegistered(Id id, UserId userId) {
    return this.registrationPersistence.exists(id, userId);
  }
}

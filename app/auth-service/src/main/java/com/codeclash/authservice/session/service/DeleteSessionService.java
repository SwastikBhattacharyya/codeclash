package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.exception.SessionNotFoundException;
import com.codeclash.authservice.session.port.inbound.DeleteSessionUseCase;
import com.codeclash.authservice.session.port.outbound.SessionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class DeleteSessionService implements DeleteSessionUseCase {

  private final SessionPersistence sessionPersistence;

  @Override
  public void delete(Id id) {
    if (!this.sessionPersistence.deleteById(id)) throw new SessionNotFoundException(id.value());
    DeleteSessionService.log
        .atInfo()
        .setMessage("session deleted")
        .log();
  }
}

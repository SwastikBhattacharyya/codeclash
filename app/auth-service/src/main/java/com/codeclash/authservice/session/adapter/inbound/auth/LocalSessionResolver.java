package com.codeclash.authservice.session.adapter.inbound.auth;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.LastUsedAt;
import com.codeclash.authservice.session.model.exception.InvalidSessionIdException;
import com.codeclash.authservice.session.model.exception.SessionNotFoundException;
import com.codeclash.authservice.session.model.request.PatchSessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.GetSessionUseCase;
import com.codeclash.authservice.session.port.inbound.PatchSessionUseCase;
import com.codeclash.common.patch.PatchObject;
import com.codeclash.contracts.auth.session.SessionPrincipal;
import com.codeclash.contracts.auth.session.SessionResolver;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalSessionResolver implements SessionResolver {

  private final GetSessionUseCase getSessionUseCase;
  private final PatchSessionUseCase patchSessionUseCase;

  @Override
  public Optional<SessionPrincipal> resolve(UUID sessionId) {
    try {
      Id id = Id.of(sessionId);
      AuthorizedSession session = this.getSessionUseCase.findById(id);
      this.patchSessionUseCase.patch(
          new PatchSessionRequest(
              id,
              PatchObject.from(JsonNullable.undefined()),
              PatchObject.of(new LastUsedAt(Instant.now()))));
      return Optional.of(
          new SessionPrincipal(
              session.session().userId().value(), Set.copyOf(session.permissions())));
    } catch (InvalidSessionIdException | SessionNotFoundException exception) {
      return Optional.empty();
    }
  }
}

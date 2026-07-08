package com.codeclash.contracts.auth.session;

import java.util.Optional;
import java.util.UUID;

public interface SessionResolver {

  Optional<SessionPrincipal> resolve(UUID sessionId);
}

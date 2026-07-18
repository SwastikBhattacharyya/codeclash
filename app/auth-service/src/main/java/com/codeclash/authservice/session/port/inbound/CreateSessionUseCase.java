package com.codeclash.authservice.session.port.inbound;

import com.codeclash.authservice.session.model.request.CreateSessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;

public interface CreateSessionUseCase {

  AuthorizedSession create(CreateSessionRequest request);
}

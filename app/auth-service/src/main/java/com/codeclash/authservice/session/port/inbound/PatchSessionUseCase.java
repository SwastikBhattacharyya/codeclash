package com.codeclash.authservice.session.port.inbound;

import com.codeclash.authservice.session.model.request.PatchSessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;

public interface PatchSessionUseCase {

  AuthorizedSession patch(PatchSessionRequest request);
}

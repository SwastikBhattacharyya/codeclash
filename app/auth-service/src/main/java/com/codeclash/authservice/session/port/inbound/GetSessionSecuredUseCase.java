package com.codeclash.authservice.session.port.inbound;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.result.AuthorizedSession;

public interface GetSessionSecuredUseCase {

  AuthorizedSession findById(Id id);
}

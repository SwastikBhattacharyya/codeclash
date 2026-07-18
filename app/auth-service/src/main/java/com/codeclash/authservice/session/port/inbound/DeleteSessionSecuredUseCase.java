package com.codeclash.authservice.session.port.inbound;

import com.codeclash.authservice.session.model.Id;

public interface DeleteSessionSecuredUseCase {

  void delete(Id id);
}

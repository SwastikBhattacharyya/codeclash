package com.codeclash.authservice.session.port.inbound;

import com.codeclash.authservice.session.model.Id;

public interface DeleteSessionUseCase {

  void delete(Id id);
}

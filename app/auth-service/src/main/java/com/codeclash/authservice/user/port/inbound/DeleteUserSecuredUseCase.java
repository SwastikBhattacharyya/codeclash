package com.codeclash.authservice.user.port.inbound;

import com.codeclash.authservice.user.model.Id;

public interface DeleteUserSecuredUseCase {

  void delete(Id id);
}

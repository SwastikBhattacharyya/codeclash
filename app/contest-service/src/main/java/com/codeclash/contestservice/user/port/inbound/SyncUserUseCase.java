package com.codeclash.contestservice.user.port.inbound;

import com.codeclash.contestservice.user.model.Id;
import com.codeclash.contestservice.user.model.User;

public interface SyncUserUseCase {

  void write(User user);

  void delete(Id id);
}

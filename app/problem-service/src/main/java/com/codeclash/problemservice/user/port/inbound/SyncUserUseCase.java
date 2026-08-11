package com.codeclash.problemservice.user.port.inbound;

import com.codeclash.problemservice.user.model.Id;
import com.codeclash.problemservice.user.model.User;

public interface SyncUserUseCase {

  void write(User user);

  void delete(Id id);
}

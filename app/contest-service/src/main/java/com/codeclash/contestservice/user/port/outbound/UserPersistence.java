package com.codeclash.contestservice.user.port.outbound;

import com.codeclash.contestservice.user.model.Id;
import com.codeclash.contestservice.user.model.User;

public interface UserPersistence {

  void upsert(User user);

  void deleteById(Id id);
}

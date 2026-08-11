package com.codeclash.problemservice.user.port.outbound;

import com.codeclash.problemservice.user.model.Id;
import com.codeclash.problemservice.user.model.User;

public interface UserPersistence {

  void upsert(User user);

  void deleteById(Id id);
}

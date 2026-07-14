package com.codeclash.authservice.user.port.outbound;

import com.codeclash.authservice.user.model.User;

public interface UserEventPublisher {

  void written(User user);

  void deleted(User user);
}

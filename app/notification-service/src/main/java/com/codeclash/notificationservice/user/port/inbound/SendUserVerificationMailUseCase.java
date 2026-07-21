package com.codeclash.notificationservice.user.port.inbound;

import com.codeclash.notificationservice.user.model.User;

public interface SendUserVerificationMailUseCase {

  void send(User user);
}

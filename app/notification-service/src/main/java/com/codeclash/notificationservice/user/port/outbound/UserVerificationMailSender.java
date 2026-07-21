package com.codeclash.notificationservice.user.port.outbound;

import com.codeclash.notificationservice.user.model.User;

public interface UserVerificationMailSender {

  void send(User user);
}

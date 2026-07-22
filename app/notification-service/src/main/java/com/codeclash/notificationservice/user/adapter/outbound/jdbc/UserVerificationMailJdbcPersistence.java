package com.codeclash.notificationservice.user.adapter.outbound.jdbc;

import com.codeclash.notificationservice.user.model.User;
import com.codeclash.notificationservice.user.port.outbound.UserVerificationMailPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class UserVerificationMailJdbcPersistence implements UserVerificationMailPersistence {

  private final JdbcClient jdbcClient;

  @Override
  public boolean claim(User user) {
    return this.jdbcClient
            .sql(
"""
insert into user_verification_mails(user_id, verification_token, email)
values (:userId, :verificationToken, :email)
on conflict (verification_token) do nothing
""")
            .param("userId", user.id().value())
            .param("verificationToken", user.verificationToken().value())
            .param("email", user.email().value())
            .update()
        == 1;
  }
}

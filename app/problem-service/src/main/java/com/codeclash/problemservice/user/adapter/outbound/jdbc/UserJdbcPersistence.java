package com.codeclash.problemservice.user.adapter.outbound.jdbc;

import com.codeclash.problemservice.user.model.Id;
import com.codeclash.problemservice.user.model.User;
import com.codeclash.problemservice.user.model.exception.DuplicateUsernameException;
import com.codeclash.problemservice.user.port.outbound.UserPersistence;
import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import io.micrometer.observation.annotation.Observed;
import java.sql.Timestamp;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class UserJdbcPersistence implements UserPersistence {

  private static final String UK_USERS_USERNAME = "uk_users_username";

  private final JdbcClient jdbcClient;
  private final ConstraintFinder constraintFinder;

  @Override
  public void upsert(User user) {
    try {
      this.upsertRow(user);
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          Map.of(
              UserJdbcPersistence.UK_USERS_USERNAME,
              () -> new DuplicateUsernameException(user.username().value())));
    }
  }

  private void upsertRow(User user) {
    this.jdbcClient
        .sql(
"""
insert into users(id, username, created_at, updated_at)
values (:id, :username, :createdAt, :updatedAt)
on conflict (id) do update set
  username = excluded.username,
  created_at = excluded.created_at,
  updated_at = excluded.updated_at
where users.updated_at <= excluded.updated_at
""")
        .param("id", user.id().value())
        .param("username", user.username().value())
        .param("createdAt", Timestamp.from(user.createdAt().value()))
        .param("updatedAt", Timestamp.from(user.updatedAt().value()))
        .update();
  }

  @Override
  public void deleteById(Id id) {
    this.jdbcClient
        .sql(
"""
delete from users where id = :id
""")
        .param("id", id.value())
        .update();
  }
}

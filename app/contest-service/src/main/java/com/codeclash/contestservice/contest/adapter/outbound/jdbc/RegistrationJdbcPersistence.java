package com.codeclash.contestservice.contest.adapter.outbound.jdbc;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.exception.AlreadyRegisteredException;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.exception.RegistrationUserNotFoundException;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.outbound.RegistrationPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class RegistrationJdbcPersistence implements RegistrationPersistence {

  private static final String PK_REGISTRATIONS = "pk_registrations";
  private static final String FK_REGISTRATIONS_CONTEST = "fk_registrations_contest";
  private static final String FK_REGISTRATIONS_USER = "fk_registrations_user";

  private final JdbcClient jdbcClient;
  private final ConstraintFinder constraintFinder;

  private static Map<String, Supplier<RuntimeException>> constraints(Id id, UserId userId) {
    return Map.ofEntries(
        Map.entry(
            RegistrationJdbcPersistence.PK_REGISTRATIONS,
            () -> new AlreadyRegisteredException(id.value(), userId.value())),
        Map.entry(
            RegistrationJdbcPersistence.FK_REGISTRATIONS_CONTEST,
            () -> new ContestNotFoundException(id.value())),
        Map.entry(
            RegistrationJdbcPersistence.FK_REGISTRATIONS_USER,
            () -> new RegistrationUserNotFoundException(userId.value())));
  }

  @Override
  public void insert(Id id, UserId userId) {
    try {
      this.jdbcClient
          .sql(
"""
insert into registrations(contest_id, user_id)
values (:contestId, :userId)
""")
          .param("contestId", id.value())
          .param("userId", userId.value())
          .update();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception, this.constraintFinder, RegistrationJdbcPersistence.constraints(id, userId));
    }
  }

  @Override
  public boolean delete(Id id, UserId userId) {
    return this.jdbcClient
            .sql(
"""
delete from registrations where contest_id = :contestId and user_id = :userId
""")
            .param("contestId", id.value())
            .param("userId", userId.value())
            .update()
        > 0;
  }

  @Override
  public boolean exists(Id id, UserId userId) {
    return this.jdbcClient
        .sql(
"""
select exists(
  select 1 from registrations where contest_id = :contestId and user_id = :userId)
""")
        .param("contestId", id.value())
        .param("userId", userId.value())
        .query(Boolean.class)
        .single();
  }
}

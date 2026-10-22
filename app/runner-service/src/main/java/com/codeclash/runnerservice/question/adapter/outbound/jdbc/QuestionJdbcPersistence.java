package com.codeclash.runnerservice.question.adapter.outbound.jdbc;

import com.codeclash.runnerservice.question.model.Id;
import com.codeclash.runnerservice.question.model.MaxRamLimit;
import com.codeclash.runnerservice.question.model.MaxRunTime;
import com.codeclash.runnerservice.question.model.Question;
import com.codeclash.runnerservice.question.port.outbound.QuestionPersistence;
import com.codeclash.common.timestamp.UpdatedAt;
import io.micrometer.observation.annotation.Observed;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class QuestionJdbcPersistence implements QuestionPersistence {

  private final JdbcClient jdbcClient;

  private static Question mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new Question(
        Id.unsafe(rs.getObject("id", UUID.class)),
        MaxRunTime.unsafe(rs.getInt("max_run_time")),
        MaxRamLimit.unsafe(rs.getInt("max_ram_limit")),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }

  @Override
  public void upsert(Question question) {
    this.jdbcClient
        .sql(
"""
insert into questions(id, max_run_time, max_ram_limit, updated_at)
values (:id, :maxRunTime, :maxRamLimit, :updatedAt)
on conflict (id) do update set
  max_run_time = excluded.max_run_time,
  max_ram_limit = excluded.max_ram_limit,
  updated_at = excluded.updated_at
where questions.updated_at <= excluded.updated_at
""")
        .param("id", question.id().value())
        .param("maxRunTime", question.maxRunTime().value())
        .param("maxRamLimit", question.maxRamLimit().value())
        .param("updatedAt", Timestamp.from(question.updatedAt().value()))
        .update();
  }

  @Override
  public void deleteById(Id id) {
    this.jdbcClient
        .sql(
"""
delete from questions where id = :id
""")
        .param("id", id.value())
        .update();
  }

  @Override
  public Optional<Question> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, max_run_time, max_ram_limit, updated_at from questions where id = :id
""")
        .param("id", id.value())
        .query(QuestionJdbcPersistence::mapRow)
        .optional();
  }
}

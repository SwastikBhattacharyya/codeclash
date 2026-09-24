package com.codeclash.contestservice.question.adapter.outbound.jdbc;

import com.codeclash.contestservice.question.model.Id;
import com.codeclash.contestservice.question.model.Question;
import com.codeclash.contestservice.question.port.outbound.QuestionPersistence;
import io.micrometer.observation.annotation.Observed;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class QuestionJdbcPersistence implements QuestionPersistence {

  private final JdbcClient jdbcClient;

  @Override
  public void upsert(Question question) {
    this.jdbcClient
        .sql(
"""
insert into questions(id, updated_at)
values (:id, :updatedAt)
on conflict (id) do update set
  updated_at = excluded.updated_at
where questions.updated_at <= excluded.updated_at
""")
        .param("id", question.id().value())
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
}

package com.codeclash.runnerservice.testcase.adapter.outbound.jdbc;

import com.codeclash.runnerservice.testcase.model.Id;
import com.codeclash.runnerservice.testcase.model.Input;
import com.codeclash.runnerservice.testcase.model.Output;
import com.codeclash.runnerservice.testcase.model.Public;
import com.codeclash.runnerservice.testcase.model.QuestionId;
import com.codeclash.runnerservice.testcase.model.TestCase;
import com.codeclash.runnerservice.testcase.model.exception.QuestionNotFoundException;
import com.codeclash.runnerservice.testcase.port.outbound.TestCasePersistence;
import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import io.micrometer.observation.annotation.Observed;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class TestCaseJdbcPersistence implements TestCasePersistence {

  private static final String FK_TEST_CASES_QUESTION = "fk_test_cases_question";

  private final JdbcClient jdbcClient;
  private final ConstraintFinder constraintFinder;

  private static TestCase mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new TestCase(
        Id.unsafe(rs.getObject("id", UUID.class)),
        new Input(rs.getString("input")),
        new Output(rs.getString("output")),
        new Public(rs.getBoolean("is_public")),
        QuestionId.unsafe(rs.getObject("question_id", UUID.class)),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }

  @Override
  public void upsert(TestCase testCase) {
    try {
      this.jdbcClient
          .sql(
"""
insert into test_cases(id, input, output, is_public, question_id, created_at, updated_at)
values (:id, :input, :output, :isPublic, :questionId, :createdAt, :updatedAt)
on conflict (id) do update set
  input = excluded.input,
  output = excluded.output,
  is_public = excluded.is_public,
  question_id = excluded.question_id,
  created_at = excluded.created_at,
  updated_at = excluded.updated_at
where test_cases.updated_at <= excluded.updated_at
""")
          .param("id", testCase.id().value())
          .param("input", testCase.input().value())
          .param("output", testCase.output().value())
          .param("isPublic", testCase.isPublic().value())
          .param("questionId", testCase.questionId().value())
          .param("createdAt", Timestamp.from(testCase.createdAt().value()))
          .param("updatedAt", Timestamp.from(testCase.updatedAt().value()))
          .update();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          Map.of(
              TestCaseJdbcPersistence.FK_TEST_CASES_QUESTION,
              () -> new QuestionNotFoundException(testCase.questionId().value())));
    }
  }

  @Override
  public void deleteById(Id id) {
    this.jdbcClient
        .sql(
"""
delete from test_cases where id = :id
""")
        .param("id", id.value())
        .update();
  }

  @Override
  public List<TestCase> findByQuestionId(QuestionId questionId) {
    return this.jdbcClient
        .sql(
"""
select id, input, output, is_public, question_id, created_at, updated_at
from test_cases
where question_id = :questionId
order by created_at, id
""")
        .param("questionId", questionId.value())
        .query(TestCaseJdbcPersistence::mapRow)
        .list();
  }
}

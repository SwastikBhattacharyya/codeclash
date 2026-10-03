package com.codeclash.contestservice.submission.adapter.outbound.jdbc;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contestservice.submission.model.Id;
import com.codeclash.contestservice.submission.model.Submission;
import com.codeclash.contestservice.submission.model.result.SyncSubmissionResult;
import com.codeclash.contestservice.submission.port.outbound.SubmissionPersistence;
import com.codeclash.contracts.problem.submission.SubmissionState;
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
public class SubmissionJdbcPersistence implements SubmissionPersistence {

  private final JdbcClient jdbcClient;

  private static Submission mapSubmission(ResultSet rs, int rowNum) throws SQLException {
    return new Submission(
        Id.unsafe(rs.getObject("id", UUID.class)),
        rs.getObject("question_id", UUID.class),
        rs.getObject("user_id", UUID.class),
        SubmissionState.from(rs.getString("state")),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }

  @Override
  public Optional<SyncSubmissionResult> upsert(Submission submission) {
    return this.jdbcClient
        .sql(
"""
insert into submissions(id, question_id, user_id, state, created_at, updated_at)
values (:id, :questionId, :userId, :state, :createdAt, :updatedAt)
on conflict (id) do update set
  question_id = excluded.question_id,
  user_id = excluded.user_id,
  state = excluded.state,
  created_at = excluded.created_at,
  updated_at = excluded.updated_at
where submissions.updated_at <= excluded.updated_at
returning (state = :correct) <> coalesce(old.state = :correct, false) as score_changed
""")
        .param("id", submission.id().value())
        .param("questionId", submission.questionId())
        .param("userId", submission.userId())
        .param("state", submission.state().value())
        .param("createdAt", Timestamp.from(submission.createdAt().value()))
        .param("updatedAt", Timestamp.from(submission.updatedAt().value()))
        .param("correct", SubmissionState.CORRECT.value())
        .query((rs, rowNum) -> new SyncSubmissionResult(rs.getBoolean("score_changed")))
        .optional();
  }

  @Override
  public Optional<Submission> deleteById(Id id) {
    return this.jdbcClient
        .sql(
"""
delete from submissions where id = :id
returning id, question_id, user_id, state, created_at, updated_at
""")
        .param("id", id.value())
        .query(SubmissionJdbcPersistence::mapSubmission)
        .optional();
  }
}

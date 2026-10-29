package com.codeclash.runnerservice.submission.adapter.outbound.jdbc;

import com.codeclash.runnerservice.submission.model.Code;
import com.codeclash.runnerservice.submission.model.ExhaustedJob;
import com.codeclash.runnerservice.submission.model.Id;
import com.codeclash.runnerservice.submission.model.Job;
import com.codeclash.runnerservice.submission.model.JobCount;
import com.codeclash.runnerservice.submission.model.JobStatus;
import com.codeclash.runnerservice.submission.model.QuestionId;
import com.codeclash.runnerservice.submission.model.TraceCarrier;
import com.codeclash.runnerservice.submission.model.exception.QuestionNotFoundException;
import com.codeclash.runnerservice.submission.model.request.EnqueueSubmissionRequest;
import com.codeclash.runnerservice.submission.port.outbound.JobPersistence;
import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import io.micrometer.observation.annotation.Observed;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Repository
@Observed
@RequiredArgsConstructor
public class JobJdbcPersistence implements JobPersistence {

  private static final String FK_SUBMISSIONS_QUESTION = "fk_submissions_question";
  private static final TypeReference<Map<String, String>> HEADERS_TYPE = new TypeReference<>() {};

  private final JdbcClient jdbcClient;
  private final ConstraintFinder constraintFinder;
  private final JsonMapper jsonMapper;

  private Job mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new Job(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Language.from(rs.getString("language")),
        new Code(rs.getString("code")),
        QuestionId.unsafe(rs.getObject("question_id", UUID.class)),
        JobStatus.from(rs.getString("status")),
        rs.getInt("attempts"),
        this.toTraceCarrier(rs.getString("trace_context")),
        Duration.ofMillis(rs.getLong("queued_for_ms")));
  }

  private ExhaustedJob mapExhaustedRow(ResultSet rs, int rowNum) throws SQLException {
    return new ExhaustedJob(
        Id.unsafe(rs.getObject("id", UUID.class)),
        this.toTraceCarrier(rs.getString("trace_context")));
  }

  private TraceCarrier toTraceCarrier(String json) {
    return new TraceCarrier(this.jsonMapper.readValue(json, JobJdbcPersistence.HEADERS_TYPE));
  }

  @Override
  public boolean insert(EnqueueSubmissionRequest request, TraceCarrier traceCarrier) {
    try {
      return this.jdbcClient
              .sql(
"""
insert into submissions(id, language, code, question_id, status, trace_context)
values (:id, :language, :code, :questionId, 'queued', cast(:traceContext as jsonb))
on conflict (id) do nothing
""")
              .param("id", request.id().value())
              .param("language", request.language().value())
              .param("code", request.code().value())
              .param("questionId", request.questionId().value())
              .param("traceContext", this.jsonMapper.writeValueAsString(traceCarrier.headers()))
              .update()
          == 1;
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          Map.of(
              JobJdbcPersistence.FK_SUBMISSIONS_QUESTION,
              () -> new QuestionNotFoundException(request.questionId().value())));
    }
  }

  @Override
  public boolean deleteIfNotRunning(Id id) {
    return this.jdbcClient
            .sql(
"""
delete from submissions where id = :id and status <> 'running'
""")
            .param("id", id.value())
            .update()
        == 1;
  }

  @Override
  public List<ExhaustedJob> completeExhausted(Duration lease, int maxAttempts) {
    return this.jdbcClient
        .sql(
"""
update submissions
set status = 'done', result = 'runner_error', claimed_at = null
where status = 'running'
  and claimed_at < now() - :leaseMillis * interval '1 millisecond'
  and attempts >= :maxAttempts
returning id, trace_context
""")
        .param("leaseMillis", lease.toMillis())
        .param("maxAttempts", maxAttempts)
        .query(this::mapExhaustedRow)
        .list();
  }

  @Override
  public Optional<Job> claimNext(Duration lease, int maxAttempts) {
    return this.jdbcClient
        .sql(
"""
update submissions
set status = 'running', claimed_at = now(), attempts = attempts + 1
where id = (
  select id from submissions
  where status = 'queued'
     or (status = 'running'
         and claimed_at < now() - :leaseMillis * interval '1 millisecond'
         and attempts < :maxAttempts)
  order by created_at, id
  for update skip locked
  limit 1
)
returning id, language, code, question_id, status, attempts, trace_context,
  cast(extract(epoch from now() - created_at) * 1000 as bigint) as queued_for_ms
""")
        .param("leaseMillis", lease.toMillis())
        .param("maxAttempts", maxAttempts)
        .query(this::mapRow)
        .optional();
  }

  @Override
  public List<JobCount> countByStatus() {
    return this.jdbcClient
        .sql(
"""
select status, count(*) as total
from submissions
where status in ('queued', 'running')
group by status
""")
        .query(
            (rs, rowNum) -> new JobCount(JobStatus.from(rs.getString("status")), rs.getLong("total")))
        .list();
  }

  @Override
  public void release(Id id) {
    this.jdbcClient
        .sql(
"""
update submissions set status = 'queued', claimed_at = null
where id = :id and status = 'running'
""")
        .param("id", id.value())
        .update();
  }

  @Override
  public boolean complete(Id id, SubmissionState result) {
    return this.jdbcClient
            .sql(
"""
update submissions set status = 'done', result = :result, claimed_at = null
where id = :id and status = 'running'
""")
            .param("id", id.value())
            .param("result", result.value())
            .update()
        == 1;
  }
}

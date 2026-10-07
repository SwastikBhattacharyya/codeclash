package com.codeclash.problemservice.contest.adapter.outbound.jdbc;

import com.codeclash.problemservice.contest.model.Contest;
import com.codeclash.problemservice.contest.model.Id;
import com.codeclash.problemservice.contest.model.result.SyncContestResult;
import com.codeclash.problemservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class ContestJdbcPersistence implements ContestPersistence {

  private final JdbcClient jdbcClient;

  private static Timestamp timestamp(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }

  @Override
  public Optional<SyncContestResult> upsert(Contest contest) {
    return this.jdbcClient
        .sql(
"""
insert into contests(id, live_at, end_at, opened_at, created_at, updated_at)
values (:id, :liveAt, :endAt, :openedAt, :createdAt, :updatedAt)
on conflict (id) do update set
  live_at = excluded.live_at,
  end_at = excluded.end_at,
  opened_at = excluded.opened_at,
  created_at = excluded.created_at,
  updated_at = excluded.updated_at
where contests.updated_at <= excluded.updated_at
returning opened_at is not null and old.opened_at is null as opened
""")
        .param("id", contest.id().value())
        .param("liveAt", ContestJdbcPersistence.timestamp(contest.liveAt()))
        .param("endAt", ContestJdbcPersistence.timestamp(contest.endAt()))
        .param("openedAt", ContestJdbcPersistence.timestamp(contest.openedAt()))
        .param("createdAt", Timestamp.from(contest.createdAt().value()))
        .param("updatedAt", Timestamp.from(contest.updatedAt().value()))
        .query((rs, rowNum) -> new SyncContestResult(rs.getBoolean("opened")))
        .optional();
  }

  @Override
  public void replaceQuestions(Id id, List<UUID> questionIds) {
    this.jdbcClient
        .sql(
"""
delete from contest_questions where contest_id = :contestId
""")
        .param("contestId", id.value())
        .update();

    if (questionIds.isEmpty()) return;

    this.jdbcClient
        .sql(
"""
insert into contest_questions(contest_id, question_id)
select :contestId, q.id
from questions q
where q.id = any(array[:questionIds]::uuid[])
""")
        .param("contestId", id.value())
        .param("questionIds", questionIds)
        .update();
  }

  @Override
  public void deleteById(Id id) {
    this.jdbcClient
        .sql(
"""
delete from contests where id = :id
""")
        .param("id", id.value())
        .update();
  }

  @Override
  public List<UUID> findRunningIds(UUID questionId, Instant now) {
    return this.jdbcClient
        .sql(
"""
select c.id
from contests c
join contest_questions cq on cq.contest_id = c.id
where cq.question_id = :questionId
  and (c.opened_at is not null or c.live_at <= :now)
  and c.end_at > :now
order by c.id
""")
        .param("questionId", questionId)
        .param("now", Timestamp.from(now))
        .query(UUID.class)
        .list();
  }
}

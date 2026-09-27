package com.codeclash.contestservice.contest.adapter.outbound.jdbc;

import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.leaderboard.LeaderboardEntry;
import com.codeclash.contestservice.contest.model.registration.UserId;
import com.codeclash.contestservice.contest.port.outbound.LeaderboardPersistence;
import com.codeclash.contracts.problem.submission.SubmissionState;
import io.micrometer.observation.annotation.Observed;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class LeaderboardJdbcPersistence implements LeaderboardPersistence {

  private final JdbcClient jdbcClient;

  private static LeaderboardEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
    Timestamp lastSolveAt = rs.getTimestamp("last_solve_at");
    return new LeaderboardEntry(
        rs.getObject("user_id", UUID.class),
        rs.getString("username"),
        rs.getInt("points"),
        rs.getInt("solved"),
        lastSolveAt == null ? null : lastSolveAt.toInstant(),
        rs.getInt("rank"));
  }

  @Override
  public void rebuild(Id id) {
    this.jdbcClient
        .sql(
"""
delete from leaderboard_entries where contest_id = :contestId
""")
        .param("contestId", id.value())
        .update();

    this.jdbcClient
        .sql(
"""
with solves as (
  select r.user_id, cq.points, min(s.created_at) as solved_at
  from contests c
  join registrations r on r.contest_id = c.id
  join contest_questions cq on cq.contest_id = c.id
  join submissions s on s.user_id = r.user_id and s.question_id = cq.question_id
  where c.id = :contestId
    and s.state = :correct
    and s.created_at >= greatest(c.live_at, r.registered_at)
    and s.created_at < c.end_at
  group by r.user_id, cq.question_id, cq.points
),
totals as (
  select r.user_id,
    coalesce(sum(solves.points), 0)::int as points,
    count(solves.user_id)::int as solved,
    max(solves.solved_at) as last_solve_at
  from registrations r
  left join solves on solves.user_id = r.user_id
  where r.contest_id = :contestId
  group by r.user_id
)
insert into leaderboard_entries(contest_id, user_id, points, solved, last_solve_at, rank)
select :contestId, user_id, points, solved, last_solve_at,
  rank() over (order by points desc, last_solve_at asc nulls last)
from totals
""")
        .param("contestId", id.value())
        .param("correct", SubmissionState.CORRECT.value())
        .update();
  }

  @Override
  public PagedEntity<LeaderboardEntry> page(Id id, PaginationRequest pagination) {
    JdbcFilter filter = JdbcFilters.equalTo("contest_id", id.value());
    JdbcPage page = JdbcPages.limitOffset(pagination);

    String countSql = JdbcPages.count("leaderboard_entries", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select user_id,
          (select u.username from users u where u.id = leaderboard_entries.user_id) as username,
          points, solved, last_solve_at, rank
        from leaderboard_entries
        """
            + filter.whereClause()
            + "order by rank, user_id\n"
            + page.sql();

    List<LeaderboardEntry> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(LeaderboardJdbcPersistence::mapEntry)
            .list();

    return JdbcPages.toPagedEntity(data, items, pagination);
  }

  @Override
  public List<Id> findCandidateIds(UUID questionId, Instant submittedAt) {
    return this.jdbcClient
        .sql(
"""
select c.id
from contests c
join contest_questions cq on cq.contest_id = c.id and cq.question_id = :questionId
where c.live_at <= :submittedAt
""")
        .param("questionId", questionId)
        .param("submittedAt", Timestamp.from(submittedAt))
        .query(UUID.class)
        .list()
        .stream()
        .map(Id::unsafe)
        .toList();
  }

  @Override
  public List<Id> findAffectedIds(UUID questionId, UserId userId, Instant submittedAt) {
    return this.jdbcClient
        .sql(
"""
select c.id
from contests c
join contest_questions cq on cq.contest_id = c.id and cq.question_id = :questionId
join registrations r on r.contest_id = c.id and r.user_id = :userId
where :submittedAt >= greatest(c.live_at, r.registered_at)
  and :submittedAt < c.end_at
""")
        .param("questionId", questionId)
        .param("userId", userId.value())
        .param("submittedAt", Timestamp.from(submittedAt))
        .query(UUID.class)
        .list()
        .stream()
        .map(Id::unsafe)
        .toList();
  }

  @Override
  public List<Id> findRegisteredIds(UserId userId) {
    return this.jdbcClient
        .sql(
"""
select contest_id from registrations where user_id = :userId
""")
        .param("userId", userId.value())
        .query(UUID.class)
        .list()
        .stream()
        .map(Id::unsafe)
        .toList();
  }
}

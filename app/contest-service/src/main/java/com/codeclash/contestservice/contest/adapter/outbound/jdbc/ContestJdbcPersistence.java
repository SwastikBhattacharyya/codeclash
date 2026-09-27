package com.codeclash.contestservice.contest.adapter.outbound.jdbc;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.EndAt;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.LiveAt;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.exception.DescriptionTooLongException;
import com.codeclash.contestservice.contest.model.exception.DescriptionTooShortException;
import com.codeclash.contestservice.contest.model.exception.DuplicateContestNameException;
import com.codeclash.contestservice.contest.model.exception.DuplicateQuestionsException;
import com.codeclash.contestservice.contest.model.exception.EndAtNotAfterLiveAtException;
import com.codeclash.contestservice.contest.model.exception.InvalidContestIdException;
import com.codeclash.contestservice.contest.model.exception.InvalidDescriptionException;
import com.codeclash.contestservice.contest.model.exception.InvalidNameException;
import com.codeclash.contestservice.contest.model.exception.NameTooLongException;
import com.codeclash.contestservice.contest.model.exception.NameTooShortException;
import com.codeclash.contestservice.contest.model.exception.PointsTooHighException;
import com.codeclash.contestservice.contest.model.exception.PointsTooLowException;
import com.codeclash.contestservice.contest.model.exception.QuestionsNotFoundException;
import com.codeclash.contestservice.contest.model.question.ContestQuestion;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;
import com.codeclash.contestservice.contest.model.request.CreateContestRequest;
import com.codeclash.contestservice.contest.model.request.PatchContestRequest;
import com.codeclash.contestservice.contest.model.request.QueryContestRequest;
import com.codeclash.contestservice.contest.model.request.ReplaceContestRequest;
import com.codeclash.contestservice.contest.model.result.ContestWriteResult;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class ContestJdbcPersistence implements ContestPersistence {

  private static final String UK_CONTESTS_NAME = "uk_contests_name";
  private static final String CK_CONTESTS_ID_VERSION = "ck_contests_id_version";
  private static final String CK_CONTESTS_NAME_MIN_SIZE = "ck_contests_name_min_size";
  private static final String CK_CONTESTS_NAME_MAX_SIZE = "ck_contests_name_max_size";
  private static final String CK_CONTESTS_NAME_SINGLE_LINE = "ck_contests_name_single_line";
  private static final String CK_CONTESTS_NAME_TRIMMED = "ck_contests_name_trimmed";
  private static final String CK_CONTESTS_DESCRIPTION_MIN_SIZE = "ck_contests_description_min_size";
  private static final String CK_CONTESTS_DESCRIPTION_MAX_SIZE = "ck_contests_description_max_size";
  private static final String CK_CONTESTS_DESCRIPTION_TRIMMED = "ck_contests_description_trimmed";
  private static final String CK_CONTESTS_END_AT_AFTER_LIVE_AT = "ck_contests_end_at_after_live_at";
  private static final String PK_CONTEST_QUESTIONS = "pk_contest_questions";
  private static final String FK_CONTEST_QUESTIONS_CONTEST = "fk_contest_questions_contest";
  private static final String FK_CONTEST_QUESTIONS_QUESTION = "fk_contest_questions_question";
  private static final String CK_CONTEST_QUESTIONS_POINTS_MIN_SIZE =
      "ck_contest_questions_points_min_size";
  private static final String CK_CONTEST_QUESTIONS_POINTS_MAX_SIZE =
      "ck_contest_questions_points_max_size";

  private static final String OLD_PREFIX = "old_";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "name", "lower(name)",
          "live_at", "live_at",
          "end_at", "end_at",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final ContestJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<Contest> query(QueryContestRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.containsAny("name", request.names()),
            JdbcFilters.containsAny("description", request.descriptions()),
            JdbcFilters.between("live_at", request.minLiveAt(), request.maxLiveAt()),
            JdbcFilters.between("end_at", request.minEndAt(), request.maxEndAt()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy = JdbcSorts.orderBy(request.sort(), ContestJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("contests", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, name, description, live_at, end_at, opened_at,
          array(select cq.question_id from contest_questions cq
                where cq.contest_id = contests.id order by cq.question_id) as question_ids,
          array(select cq.points from contest_questions cq
                where cq.contest_id = contests.id order by cq.question_id) as question_points,
          created_at, updated_at
        from contests
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Contest> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<Contest> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, live_at, end_at, opened_at,
  array(select cq.question_id from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_ids,
  array(select cq.points from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_points,
  created_at, updated_at
from contests
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Optional<Contest> lockById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, live_at, end_at, opened_at,
  array(select cq.question_id from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_ids,
  array(select cq.points from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_points,
  created_at, updated_at
from contests
where id = :id
for no key update
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Contest create(CreateContestRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into contests(name, description, live_at, end_at)
values (:name, :description, :liveAt, :endAt)
returning id, name, description, live_at, end_at, opened_at, created_at, updated_at
""")
          .param("name", request.name().value())
          .param("description", request.description().value())
          .param("liveAt", Timestamp.from(request.liveAt().value()))
          .param("endAt", Timestamp.from(request.endAt().value()))
          .query((rs, rowNum) -> this.rowMapper.mapRow(rs, "", ContestJdbcPersistence.none()))
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          ContestJdbcPersistence.contestConstraints(
              null,
              request.name().value(),
              request.description().value(),
              request.liveAt().value(),
              request.endAt().value()));
    }
  }

  @Override
  public ContestWriteResult replace(ReplaceContestRequest request) {
    try {
      ContestWriteResult written =
          this.jdbcClient
              .sql(
"""
insert into contests(id, name, description, live_at, end_at)
values (:id, :name, :description, :liveAt, :endAt)
on conflict (id) do update set
  name = excluded.name,
  description = excluded.description,
  live_at = excluded.live_at,
  end_at = excluded.end_at
returning id, name, description, live_at, end_at, opened_at, created_at, updated_at,
  old.id as old_id, old.name as old_name, old.description as old_description,
  old.live_at as old_live_at, old.end_at as old_end_at, old.opened_at as old_opened_at,
  old.created_at as old_created_at, old.updated_at as old_updated_at
""")
              .param("id", request.id().value())
              .param("name", request.name().value())
              .param("description", request.description().value())
              .param("liveAt", Timestamp.from(request.liveAt().value()))
              .param("endAt", Timestamp.from(request.endAt().value()))
              .query(this::mapWrite)
              .single();
      return this.withStoredQuestions(request.id(), written);
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          ContestJdbcPersistence.contestConstraints(
              request.id().value(),
              request.name().value(),
              request.description().value(),
              request.liveAt().value(),
              request.endAt().value()));
    }
  }

  @Override
  public Optional<ContestWriteResult> patch(PatchContestRequest request) {
    String name = request.name().map(Name::value).orElse(null);
    String description = request.description().map(Description::value).orElse(null);
    Instant liveAt = request.liveAt().map(LiveAt::value).orElse(null);
    Instant endAt = request.endAt().map(EndAt::value).orElse(null);
    try {
      Optional<ContestWriteResult> written =
          this.jdbcClient
              .sql(
"""
update contests set
  name = coalesce(:name, name),
  description = coalesce(:description, description),
  live_at = coalesce(cast(:liveAt as timestamptz), live_at),
  end_at = coalesce(cast(:endAt as timestamptz), end_at)
where id = :id
returning id, name, description, live_at, end_at, opened_at, created_at, updated_at,
  old.id as old_id, old.name as old_name, old.description as old_description,
  old.live_at as old_live_at, old.end_at as old_end_at, old.opened_at as old_opened_at,
  old.created_at as old_created_at, old.updated_at as old_updated_at
""")
              .param("id", request.id().value())
              .param("name", name)
              .param("description", description)
              .param("liveAt", liveAt == null ? null : Timestamp.from(liveAt))
              .param("endAt", endAt == null ? null : Timestamp.from(endAt))
              .query(this::mapWrite)
              .optional();
      return written.map(result -> this.withStoredQuestions(request.id(), result));
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          ContestJdbcPersistence.contestConstraints(
              request.id().value(), name, description, liveAt, endAt));
    }
  }

  @Override
  public void replaceQuestions(Id id, ContestQuestions questions) {
    List<UUID> questionIds = questions.questionIds();
    List<Integer> points = questions.points();
    try {
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
insert into contest_questions(contest_id, question_id, points)
select :contestId, q.question_id, q.points
from unnest(array[:questionIds]::uuid[], array[:points]::integer[]) as q(question_id, points)
""")
          .param("contestId", id.value())
          .param("questionIds", questionIds)
          .param("points", points)
          .update();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          ContestJdbcPersistence.questionConstraints(id.value(), questionIds, points));
    }
  }

  @Override
  public List<Contest> openDue(Instant cutoff, int limit) {
    return this.jdbcClient
        .sql(
"""
update contests set opened_at = now()
where id in (
  select id from contests
  where opened_at is null and live_at <= :cutoff
  order by live_at
  limit :limit
  for update skip locked)
returning id, name, description, live_at, end_at, opened_at,
  array(select cq.question_id from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_ids,
  array(select cq.points from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_points,
  created_at, updated_at
""")
        .param("cutoff", Timestamp.from(cutoff))
        .param("limit", limit)
        .query(this.rowMapper)
        .list();
  }

  @Override
  public Optional<Contest> deleteById(Id id) {
    return this.jdbcClient
        .sql(
"""
delete from contests where id = :id
returning id, name, description, live_at, end_at, opened_at,
  array(select cq.question_id from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_ids,
  array(select cq.points from contest_questions cq where cq.contest_id = contests.id
      order by cq.question_id) as question_points,
  created_at, updated_at
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  private ContestWriteResult mapWrite(ResultSet rs, int rowNum) throws SQLException {
    Contest current = this.rowMapper.mapRow(rs, "", ContestJdbcPersistence.none());
    if (rs.getObject(ContestJdbcPersistence.OLD_PREFIX + "id", UUID.class) == null)
      return new ContestWriteResult(null, current);
    Contest previous =
        this.rowMapper.mapRow(rs, ContestJdbcPersistence.OLD_PREFIX, ContestJdbcPersistence.none());
    return new ContestWriteResult(previous, current);
  }

  private ContestWriteResult withStoredQuestions(Id id, ContestWriteResult written) {
    if (written.previous() == null) return written;
    ContestQuestions stored = this.findQuestions(id);
    return new ContestWriteResult(
        written.previous().withQuestions(stored), written.current().withQuestions(stored));
  }

  private ContestQuestions findQuestions(Id id) {
    List<ContestQuestion> questions =
        this.jdbcClient
            .sql(
"""
select question_id, points from contest_questions
where contest_id = :contestId
order by question_id
""")
            .param("contestId", id.value())
            .query(
                (rs, rowNum) ->
                    ContestQuestion.unsafe(
                        rs.getObject("question_id", UUID.class), rs.getInt("points")))
            .list();
    return ContestQuestions.unsafe(questions);
  }

  private static ContestQuestions none() {
    return ContestQuestions.unsafe(List.of());
  }

  private static Map<String, Supplier<RuntimeException>> contestConstraints(
      UUID id, String name, String description, Instant liveAt, Instant endAt) {
    return Map.ofEntries(
        Map.entry(
            ContestJdbcPersistence.UK_CONTESTS_NAME,
            () -> new DuplicateContestNameException(name)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_ID_VERSION,
            () -> new InvalidContestIdException(id)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_NAME_MIN_SIZE,
            () -> new NameTooShortException(name)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_NAME_MAX_SIZE,
            () -> new NameTooLongException(name)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_NAME_SINGLE_LINE,
            () -> new InvalidNameException(name)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_NAME_TRIMMED,
            () -> new InvalidNameException(name)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_DESCRIPTION_MIN_SIZE,
            () -> new DescriptionTooShortException(description)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_DESCRIPTION_MAX_SIZE,
            () -> new DescriptionTooLongException(description)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_DESCRIPTION_TRIMMED,
            () -> new InvalidDescriptionException(description)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTESTS_END_AT_AFTER_LIVE_AT,
            () -> new EndAtNotAfterLiveAtException(liveAt, endAt)));
  }

  private static Map<String, Supplier<RuntimeException>> questionConstraints(
      UUID id, List<UUID> questionIds, List<Integer> points) {
    return Map.ofEntries(
        Map.entry(
            ContestJdbcPersistence.PK_CONTEST_QUESTIONS,
            () -> new DuplicateQuestionsException(questionIds)),
        Map.entry(
            ContestJdbcPersistence.FK_CONTEST_QUESTIONS_CONTEST,
            () -> new ContestNotFoundException(id)),
        Map.entry(
            ContestJdbcPersistence.FK_CONTEST_QUESTIONS_QUESTION,
            () -> new QuestionsNotFoundException(questionIds)),
        Map.entry(
            ContestJdbcPersistence.CK_CONTEST_QUESTIONS_POINTS_MIN_SIZE,
            () -> new PointsTooLowException(Collections.min(points))),
        Map.entry(
            ContestJdbcPersistence.CK_CONTEST_QUESTIONS_POINTS_MAX_SIZE,
            () -> new PointsTooHighException(Collections.max(points))));
  }
}

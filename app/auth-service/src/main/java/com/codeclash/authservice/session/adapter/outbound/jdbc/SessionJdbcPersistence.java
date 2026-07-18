package com.codeclash.authservice.session.adapter.outbound.jdbc;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.authservice.session.model.exception.InvalidSessionIdException;
import com.codeclash.authservice.session.model.exception.UserNotFoundException;
import com.codeclash.authservice.session.model.request.PatchSessionRequest;
import com.codeclash.authservice.session.model.request.QuerySessionRequest;
import com.codeclash.authservice.session.port.outbound.SessionPersistence;
import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import io.micrometer.observation.annotation.Observed;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
public class SessionJdbcPersistence implements SessionPersistence {

  private static final String CK_SESSIONS_ID_VERSION = "ck_sessions_id_version";
  private static final String FK_SESSIONS_USER = "fk_sessions_user";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "last_used_at", "last_used_at",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final SessionJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<Session> query(QuerySessionRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.in("user_id", request.userIds().stream().map(UserId::value).toList()),
            JdbcFilters.between("last_used_at", request.minLastUsedAt(), request.maxLastUsedAt()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy =
        JdbcSorts.orderBy(request.sort(), SessionJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("sessions", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, user_id, last_used_at, created_at, updated_at
        from sessions
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Session> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<Session> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, user_id, last_used_at, created_at, updated_at
from sessions
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Session create(UserId userId) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into sessions(user_id)
values (:userId)
returning id, user_id, last_used_at, created_at, updated_at
""")
          .param("userId", userId.value())
          .query(this.rowMapper)
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          SessionJdbcPersistence.constraints(null, userId.value()));
    }
  }

  @Override
  public Optional<Session> patch(PatchSessionRequest request) {
    UUID userId = request.userId().map(UserId::value).orElse(null);
    OffsetDateTime lastUsedAt =
        request
            .lastUsedAt()
            .map(value -> value.value().atOffset(ZoneOffset.UTC))
            .orElse(null);
    try {
      return this.jdbcClient
          .sql(
"""
update sessions set
  user_id = coalesce(:userId, user_id),
  last_used_at = coalesce(:lastUsedAt, last_used_at)
where id = :id
returning id, user_id, last_used_at, created_at, updated_at
""")
          .param("id", request.id().value())
          .param("userId", userId)
          .param("lastUsedAt", lastUsedAt)
          .query(this.rowMapper)
          .optional();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          SessionJdbcPersistence.constraints(request.id().value(), userId));
    }
  }

  @Override
  public boolean deleteById(Id id) {
    return this.jdbcClient
            .sql(
"""
delete from sessions where id = :id
""")
            .param("id", id.value())
            .update()
        > 0;
  }

  private static Map<String, Supplier<RuntimeException>> constraints(UUID id, UUID userId) {
    return Map.of(
        SessionJdbcPersistence.CK_SESSIONS_ID_VERSION,
        () -> new InvalidSessionIdException(id),
        SessionJdbcPersistence.FK_SESSIONS_USER,
        () -> new UserNotFoundException(userId));
  }
}

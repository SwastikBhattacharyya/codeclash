package com.codeclash.authservice.permission.adapter.outbound.jdbc;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.model.request.QueryPermissionRequest;
import com.codeclash.authservice.permission.port.outbound.PermissionPersistence;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.permission.Name;
import com.codeclash.common.permission.PermissionDefinition;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class PermissionJdbcPersistence implements PermissionPersistence {

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "name", "name",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final PermissionJdbcRowMapper rowMapper;

  @Override
  public PagedEntity<Permission> query(QueryPermissionRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.containsAny("name", request.names()),
            PermissionJdbcPersistence.hasAnyRole(request.roleIds()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy =
        JdbcSorts.orderBy(request.sort(), PermissionJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("permissions", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, name, description, created_at, updated_at
        from permissions
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Permission> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<Permission> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, created_at, updated_at
from permissions
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public List<Permission> findAllByIdIn(List<Id> ids) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, created_at, updated_at
from permissions
where id in (:ids)
order by name
""")
        .param("ids", ids.stream().map(Id::value).toList())
        .query(this.rowMapper)
        .list();
  }

  @Override
  public List<Permission> findAllByNameIn(List<Name> names) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, created_at, updated_at
from permissions
where name in (:names)
order by name
""")
        .param("names", names.stream().map(Name::value).toList())
        .query(this.rowMapper)
        .list();
  }

  @Override
  public void lock() {
    this.jdbcClient
        .sql(
"""
lock table permissions in share row exclusive mode
""")
        .update();
  }

  @Override
  public void deleteByNameNotIn(List<Name> names) {
    this.jdbcClient
        .sql(
"""
delete from permissions where name not in (:names)
""")
        .param("names", names.stream().map(Name::value).toList())
        .update();
  }

  @Override
  public void upsert(PermissionDefinition definition) {
    this.jdbcClient
        .sql(
"""
insert into permissions(name, description)
values (:name, :description)
on conflict (name) do update set description = excluded.description
where permissions.description is distinct from excluded.description
""")
        .param("name", definition.name().value())
        .param("description", definition.description().value())
        .update();
  }

  private static JdbcFilter hasAnyRole(List<UUID> roleIds) {
    if (roleIds == null || roleIds.isEmpty()) return JdbcFilters.and();
    return new JdbcFilter(
        "exists (select 1 from role_permissions rp where rp.permission_id = permissions.id"
            + " and rp.role_id in (:role_ids))",
        Map.of("role_ids", roleIds));
  }
}

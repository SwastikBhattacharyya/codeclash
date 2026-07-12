package com.codeclash.authservice.role.adapter.outbound.jdbc;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.exception.DescriptionTooLongException;
import com.codeclash.authservice.role.model.exception.DescriptionTooShortException;
import com.codeclash.authservice.role.model.exception.DuplicateRoleNameException;
import com.codeclash.authservice.role.model.exception.InvalidNameException;
import com.codeclash.authservice.role.model.exception.InvalidRoleIdException;
import com.codeclash.authservice.role.model.exception.NameTooLongException;
import com.codeclash.authservice.role.model.exception.PermissionsNotFoundException;
import com.codeclash.authservice.role.model.permission.PermissionId;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.CreateRoleRequest;
import com.codeclash.authservice.role.model.request.PatchRoleRequest;
import com.codeclash.authservice.role.model.request.QueryRoleRequest;
import com.codeclash.authservice.role.model.request.ReplaceRoleRequest;
import com.codeclash.authservice.role.model.request.SyncDefaultRoleRequest;
import com.codeclash.authservice.role.model.result.ReplaceRoleResult;
import com.codeclash.authservice.role.port.outbound.RolePersistence;
import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import io.micrometer.observation.annotation.Observed;
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
public class RoleJdbcPersistence implements RolePersistence {

  private static final String CK_ROLES_ID_VERSION = "ck_roles_id_version";
  private static final String CK_ROLES_NAME_MAX_SIZE = "ck_roles_name_max_size";
  private static final String CK_ROLES_NAME_FORMAT = "ck_roles_name_format";
  private static final String CK_ROLES_DESCRIPTION_MIN_SIZE = "ck_roles_description_min_size";
  private static final String CK_ROLES_DESCRIPTION_MAX_SIZE = "ck_roles_description_max_size";
  private static final String UK_ROLES_NAME = "uk_roles_name";
  private static final String FK_ROLE_PERMISSIONS_PERMISSION = "fk_role_permissions_permission";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "name", "name",
          "is_default", "is_default",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final RoleJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<Role<Unresolved>> query(QueryRoleRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.containsAny("name", request.names()),
            RoleJdbcPersistence.hasAnyPermission(
                request.permissionIds().stream().map(PermissionId::value).toList()),
            JdbcFilters.equalTo("is_default", request.isDefault()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy = JdbcSorts.orderBy(request.sort(), RoleJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("roles", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, name, description, is_default,
          array(select rp.permission_id from role_permissions rp where rp.role_id = roles.id
                order by rp.permission_id) as permission_ids,
          created_at, updated_at
        from roles
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Role<Unresolved>> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<Role<Unresolved>> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, is_default,
  array(select rp.permission_id from role_permissions rp where rp.role_id = roles.id
      order by rp.permission_id) as permission_ids,
  created_at, updated_at
from roles
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public List<Role<Unresolved>> findAllByIdIn(List<Id> ids) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, is_default,
  array(select rp.permission_id from role_permissions rp where rp.role_id = roles.id
      order by rp.permission_id) as permission_ids,
  created_at, updated_at
from roles
where id in (:ids)
""")
        .param("ids", ids.stream().map(Id::value).toList())
        .query(this.rowMapper)
        .list();
  }

  @Override
  public Optional<Role<Unresolved>> findByName(Name name) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, is_default,
  array(select rp.permission_id from role_permissions rp where rp.role_id = roles.id
      order by rp.permission_id) as permission_ids,
  created_at, updated_at
from roles
where name = :name
""")
        .param("name", name.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Role<Unresolved> create(CreateRoleRequest<Unresolved> request) {
    try {
      Role<Unresolved> role =
          this.jdbcClient
              .sql(
"""
insert into roles(name, description)
values (:name, :description)
returning id, name, description, is_default,
  array(select rp.permission_id from role_permissions rp where rp.role_id = roles.id
      order by rp.permission_id) as permission_ids,
  created_at, updated_at
""")
              .param("name", request.name().value())
              .param("description", request.description().value())
              .query(this.rowMapper)
              .single();
      this.replaceMappings(role.id().value(), request.permissions());
      return role.withPermissions(request.permissions());
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          RoleJdbcPersistence.constraints(
              null,
              request.name().value(),
              request.description().value(),
              RoleJdbcPersistence.ids(request.permissions())));
    }
  }

  @Override
  public Optional<ReplaceRoleResult<Unresolved>> replace(
      ReplaceRoleRequest<Unresolved> request) {
    try {
      Optional<ReplaceRoleResult<Unresolved>> result =
          this.jdbcClient
              .sql(
"""
insert into roles(id, name, description)
values (:id, :name, :description)
on conflict (id) do update set
  name = excluded.name,
  description = excluded.description
where not roles.is_default
returning id, name, description, is_default,
  array(select rp.permission_id from role_permissions rp where rp.role_id = roles.id
      order by rp.permission_id) as permission_ids,
  created_at, updated_at, old.id is null as created
""")
              .param("id", request.id().value())
              .param("name", request.name().value())
              .param("description", request.description().value())
              .query((rs, rowNum) ->
                  new ReplaceRoleResult<>(
                      this.rowMapper.mapRow(rs, rowNum), rs.getBoolean("created")))
              .optional();
      result.ifPresent(
          replaced -> this.replaceMappings(replaced.role().id().value(), request.permissions()));
      return result.map(
          replaced -> replaced.withRole(replaced.role().withPermissions(request.permissions())));
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          RoleJdbcPersistence.constraints(
              request.id().value(),
              request.name().value(),
              request.description().value(),
              RoleJdbcPersistence.ids(request.permissions())));
    }
  }

  @Override
  public Optional<Role<Unresolved>> patch(PatchRoleRequest<Unresolved> request) {
    String name = request.name().map(Name::value).orElse(null);
    String description = request.description().map(Description::value).orElse(null);
    try {
      Optional<Role<Unresolved>> role =
          this.jdbcClient
              .sql(
"""
update roles set
  name = coalesce(:name, name),
  description = coalesce(:description, description)
where id = :id and not is_default
returning id, name, description, is_default,
  array(select rp.permission_id from role_permissions rp where rp.role_id = roles.id
      order by rp.permission_id) as permission_ids,
  created_at, updated_at
""")
              .param("id", request.id().value())
              .param("name", name)
              .param("description", description)
              .query(this.rowMapper)
              .optional();
      if (role.isEmpty() || !request.permissions().isPresent()) return role;
      Permissions<Unresolved> permissions = request.permissions().orElse(null);
      this.replaceMappings(role.get().id().value(), permissions);
      return Optional.of(role.get().withPermissions(permissions));
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          RoleJdbcPersistence.constraints(
              request.id().value(),
              name,
              description,
              request.permissions().map(RoleJdbcPersistence::ids).orElse(List.of())));
    }
  }

  @Override
  public boolean deleteById(Id id) {
    return this.jdbcClient
            .sql(
"""
delete from roles where id = :id and not is_default
""")
            .param("id", id.value())
            .update()
        > 0;
  }

  @Override
  public void lock() {
    this.jdbcClient
        .sql(
"""
lock table roles in share row exclusive mode
""")
        .update();
  }

  @Override
  public void demoteDefaultsByNameNotIn(List<Name> names) {
    this.jdbcClient
        .sql(
"""
update roles set is_default = false where is_default and name not in (:names)
""")
        .param("names", names.stream().map(Name::value).toList())
        .update();
  }

  @Override
  public void upsertDefault(SyncDefaultRoleRequest<Unresolved> request) {
    this.jdbcClient
        .sql(
"""
insert into roles(name, description, is_default)
values (:name, :description, true)
on conflict (name) do update set
  description = excluded.description,
  is_default = true
where roles.description is distinct from excluded.description or not roles.is_default
""")
        .param("name", request.name().value())
        .param("description", request.description().value())
        .update();

    UUID roleId =
        this.jdbcClient
            .sql(
"""
select id from roles where name = :name
""")
            .param("name", request.name().value())
            .query(UUID.class)
            .single();
    this.replaceMappings(roleId, request.permissions());
  }

  private void replaceMappings(UUID roleId, Permissions<Unresolved> permissions) {
    this.jdbcClient
        .sql(
"""
delete from role_permissions where role_id = :roleId
""")
        .param("roleId", roleId)
        .update();

    List<UUID> permissionIds = RoleJdbcPersistence.ids(permissions);
    if (permissionIds.isEmpty()) return;

    this.jdbcClient
        .sql(
"""
insert into role_permissions(role_id, permission_id)
select :roleId, permission_id from unnest(array[:permissionIds]::uuid[]) as permission_id
""")
        .param("roleId", roleId)
        .param("permissionIds", permissionIds)
        .update();
  }

  private static List<UUID> ids(Permissions<Unresolved> permissions) {
    return permissions.value().ids().stream().map(PermissionId::value).toList();
  }

  private static JdbcFilter hasAnyPermission(List<UUID> permissionIds) {
    if (permissionIds == null || permissionIds.isEmpty()) return JdbcFilters.and();
    return new JdbcFilter(
        "exists (select 1 from role_permissions rp where rp.role_id = roles.id"
            + " and rp.permission_id in (:permission_ids))",
        Map.of("permission_ids", permissionIds));
  }

  private static Map<String, Supplier<RuntimeException>> constraints(
      UUID id, String name, String description, List<UUID> permissionIds) {
    return Map.of(
        RoleJdbcPersistence.CK_ROLES_ID_VERSION,
        () -> new InvalidRoleIdException(id),
        RoleJdbcPersistence.CK_ROLES_NAME_MAX_SIZE,
        () -> new NameTooLongException(name),
        RoleJdbcPersistence.CK_ROLES_NAME_FORMAT,
        () -> new InvalidNameException(name),
        RoleJdbcPersistence.CK_ROLES_DESCRIPTION_MIN_SIZE,
        () -> new DescriptionTooShortException(description),
        RoleJdbcPersistence.CK_ROLES_DESCRIPTION_MAX_SIZE,
        () -> new DescriptionTooLongException(description),
        RoleJdbcPersistence.UK_ROLES_NAME,
        () -> new DuplicateRoleNameException(name),
        RoleJdbcPersistence.FK_ROLE_PERMISSIONS_PERMISSION,
        () -> new PermissionsNotFoundException(permissionIds));
  }
}

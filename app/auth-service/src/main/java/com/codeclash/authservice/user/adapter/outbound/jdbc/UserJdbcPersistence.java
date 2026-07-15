package com.codeclash.authservice.user.adapter.outbound.jdbc;

import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.Name;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.exception.DuplicateEmailException;
import com.codeclash.authservice.user.model.exception.DuplicateUsernameException;
import com.codeclash.authservice.user.model.exception.EmailTooLongException;
import com.codeclash.authservice.user.model.exception.InvalidEmailException;
import com.codeclash.authservice.user.model.exception.InvalidNameException;
import com.codeclash.authservice.user.model.exception.InvalidPasswordHashException;
import com.codeclash.authservice.user.model.exception.InvalidUserIdException;
import com.codeclash.authservice.user.model.exception.InvalidUsernameException;
import com.codeclash.authservice.user.model.exception.NameTooLongException;
import com.codeclash.authservice.user.model.exception.NameTooShortException;
import com.codeclash.authservice.user.model.exception.RoleNotFoundException;
import com.codeclash.authservice.user.model.exception.UsernameTooLongException;
import com.codeclash.authservice.user.model.exception.UsernameTooShortException;
import com.codeclash.authservice.user.model.password.Hashed;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.authservice.user.model.request.CreateUserRequest;
import com.codeclash.authservice.user.model.request.PatchUserRequest;
import com.codeclash.authservice.user.model.request.QueryUserRequest;
import com.codeclash.authservice.user.model.request.ReplaceUserRequest;
import com.codeclash.authservice.user.model.result.ReplaceUserResult;
import com.codeclash.authservice.user.port.outbound.UserPersistence;
import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import io.micrometer.observation.annotation.Observed;
import java.util.HashMap;
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
public class UserJdbcPersistence implements UserPersistence {

  private static final String CK_USERS_ID_VERSION = "ck_users_id_version";
  private static final String CK_USERS_NAME_MIN_SIZE = "ck_users_name_min_size";
  private static final String CK_USERS_NAME_MAX_SIZE = "ck_users_name_max_size";
  private static final String CK_USERS_NAME_LETTERS = "ck_users_name_letters";
  private static final String CK_USERS_NAME_TRIMMED = "ck_users_name_trimmed";
  private static final String CK_USERS_USERNAME_MIN_SIZE = "ck_users_username_min_size";
  private static final String CK_USERS_USERNAME_MAX_SIZE = "ck_users_username_max_size";
  private static final String CK_USERS_USERNAME_FORMAT = "ck_users_username_format";
  private static final String CK_USERS_USERNAME_TRIMMED = "ck_users_username_trimmed";
  private static final String CK_USERS_EMAIL_MAX_SIZE = "ck_users_email_max_size";
  private static final String CK_USERS_EMAIL_FORMAT = "ck_users_email_format";
  private static final String CK_USERS_EMAIL_TRIMMED = "ck_users_email_trimmed";
  private static final String CK_USERS_PASSWORD_HASH_NOT_BLANK =
      "ck_users_password_hash_not_blank";
  private static final String UK_USERS_EMAIL_LOWER = "uk_users_email_lower";
  private static final String UK_USERS_USERNAME = "uk_users_username";
  private static final String FK_USERS_ROLE = "fk_users_role";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "name", "lower(name)",
          "username", "username",
          "email", "lower(email)",
          "verified", "is_verified",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final UserJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<User> query(QueryUserRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.containsAny("name", request.names()),
            JdbcFilters.containsAny("username", request.usernames()),
            JdbcFilters.containsAny("email", request.emails()),
            JdbcFilters.in("role_id", request.roleIds().stream().map(RoleId::value).toList()),
            JdbcFilters.equalTo("is_verified", request.verified()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy = JdbcSorts.orderBy(request.sort(), UserJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("users", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
        from users
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<User> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<User> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
from users
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return this.jdbcClient
        .sql(
"""
select id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
from users
where lower(email) = lower(:email)
""")
        .param("email", email.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Optional<User> findByUsername(Username username) {
    return this.jdbcClient
        .sql(
"""
select id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
from users
where username = :username
""")
        .param("username", username.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Map<Id, RoleId> findRoleIds(List<Id> ids) {
    Map<Id, RoleId> roleIds = new HashMap<>();
    this.jdbcClient
        .sql(
"""
select id, role_id from users where id in (:ids) and role_id is not null
""")
        .param("ids", ids.stream().map(Id::value).toList())
        .query(
            rs -> {
              roleIds.put(
                  Id.unsafe(rs.getObject("id", UUID.class)),
                  RoleId.unsafe(rs.getObject("role_id", UUID.class)));
            });
    return Map.copyOf(roleIds);
  }

  @Override
  public User create(CreateUserRequest<Hashed> request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into users(name, username, email, password_hash, is_verified, role_id)
values (:name, :username, :email, :passwordHash, :verified, :roleId)
returning id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
""")
          .param("name", request.name().value())
          .param("username", request.username().value())
          .param("email", request.email().value())
          .param("passwordHash", request.password().value())
          .param("verified", request.verified().value())
          .param("roleId", request.role().value())
          .query(this.rowMapper)
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          UserJdbcPersistence.constraints(
              null,
              request.name().value(),
              request.username().value(),
              request.email().value(),
              request.role().value()));
    }
  }

  @Override
  public ReplaceUserResult replace(ReplaceUserRequest<Hashed> request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into users(id, name, username, email, password_hash, is_verified, role_id)
values (:id, :name, :username, :email, :passwordHash, :verified, :roleId)
on conflict (id) do update set
  name = excluded.name,
  username = excluded.username,
  email = excluded.email,
  password_hash = excluded.password_hash,
  is_verified = excluded.is_verified,
  role_id = excluded.role_id
returning id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at, old.id is null as created
""")
          .param("id", request.id().value())
          .param("name", request.name().value())
          .param("username", request.username().value())
          .param("email", request.email().value())
          .param("passwordHash", request.password().value())
          .param("verified", request.verified().value())
          .param("roleId", request.role().value())
          .query((rs, rowNum) ->
              new ReplaceUserResult(this.rowMapper.mapRow(rs, rowNum), rs.getBoolean("created")))
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          UserJdbcPersistence.constraints(
              request.id().value(),
              request.name().value(),
              request.username().value(),
              request.email().value(),
              request.role().value()));
    }
  }

  @Override
  public Optional<User> patch(PatchUserRequest<Hashed> request) {
    String name = request.name().map(Name::value).orElse(null);
    String username = request.username().map(Username::value).orElse(null);
    String email = request.email().map(Email::value).orElse(null);
    UUID roleId = request.role().map(RoleId::value).orElse(null);
    try {
      return this.jdbcClient
          .sql(
"""
update users set
  name = coalesce(:name, name),
  username = coalesce(:username, username),
  email = coalesce(:email, email),
  password_hash = coalesce(:passwordHash, password_hash),
  is_verified = coalesce(:verified, is_verified),
  role_id = coalesce(:roleId, role_id)
where id = :id
returning id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
""")
          .param("id", request.id().value())
          .param("name", name)
          .param("username", username)
          .param("email", email)
          .param("passwordHash", request.password().map(Password::value).orElse(null))
          .param("verified", request.verified().map(Verified::value).orElse(null))
          .param("roleId", roleId)
          .query(this.rowMapper)
          .optional();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          UserJdbcPersistence.constraints(
              request.id().value(), name, username, email, roleId));
    }
  }

  @Override
  public Optional<User> deleteById(Id id) {
    return this.jdbcClient
        .sql(
"""
delete from users where id = :id
returning id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public List<User> assignRoleWhereMissing(RoleId role) {
    try {
      return this.jdbcClient
          .sql(
"""
update users set role_id = :roleId where role_id is null
returning id, name, username, email, password_hash, is_verified, verification_token, role_id, created_at, updated_at
""")
          .param("roleId", role.value())
          .query(this.rowMapper)
          .list();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          Map.of(UserJdbcPersistence.FK_USERS_ROLE, () -> new RoleNotFoundException(role.value())));
    }
  }

  private static Map<String, Supplier<RuntimeException>> constraints(
      UUID id, String name, String username, String email, UUID roleId) {
    return Map.ofEntries(
        Map.entry(UserJdbcPersistence.CK_USERS_ID_VERSION, () -> new InvalidUserIdException(id)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_NAME_MIN_SIZE, () -> new NameTooShortException(name)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_NAME_MAX_SIZE, () -> new NameTooLongException(name)),
        Map.entry(UserJdbcPersistence.CK_USERS_NAME_LETTERS, () -> new InvalidNameException(name)),
        Map.entry(UserJdbcPersistence.CK_USERS_NAME_TRIMMED, () -> new InvalidNameException(name)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_USERNAME_MIN_SIZE,
            () -> new UsernameTooShortException(username)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_USERNAME_MAX_SIZE,
            () -> new UsernameTooLongException(username)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_USERNAME_FORMAT,
            () -> new InvalidUsernameException(username)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_USERNAME_TRIMMED,
            () -> new InvalidUsernameException(username)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_EMAIL_MAX_SIZE, () -> new EmailTooLongException(email)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_EMAIL_FORMAT, () -> new InvalidEmailException(email)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_EMAIL_TRIMMED, () -> new InvalidEmailException(email)),
        Map.entry(
            UserJdbcPersistence.CK_USERS_PASSWORD_HASH_NOT_BLANK,
            InvalidPasswordHashException::new),
        Map.entry(
            UserJdbcPersistence.UK_USERS_EMAIL_LOWER, () -> new DuplicateEmailException(email)),
        Map.entry(
            UserJdbcPersistence.UK_USERS_USERNAME, () -> new DuplicateUsernameException(username)),
        Map.entry(UserJdbcPersistence.FK_USERS_ROLE, () -> new RoleNotFoundException(roleId)));
  }
}

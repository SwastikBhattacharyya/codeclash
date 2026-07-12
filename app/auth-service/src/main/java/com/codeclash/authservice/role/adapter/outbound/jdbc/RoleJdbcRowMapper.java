package com.codeclash.authservice.role.adapter.outbound.jdbc;

import com.codeclash.authservice.role.model.Default;
import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class RoleJdbcRowMapper implements RowMapper<Role<Unresolved>> {

  @Override
  public Role<Unresolved> mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    UUID[] permissionIds = (UUID[]) rs.getArray("permission_ids").getArray();
    return new Role<>(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Name.unsafe(rs.getString("name")),
        Description.unsafe(rs.getString("description")),
        new Default(rs.getBoolean("is_default")),
        Permissions.unresolved(List.of(permissionIds)),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

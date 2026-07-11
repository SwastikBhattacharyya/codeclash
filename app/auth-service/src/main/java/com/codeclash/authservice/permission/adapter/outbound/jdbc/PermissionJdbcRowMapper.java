package com.codeclash.authservice.permission.adapter.outbound.jdbc;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.common.permission.Description;
import com.codeclash.common.permission.Name;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class PermissionJdbcRowMapper implements RowMapper<Permission> {

  @Override
  public Permission mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    return new Permission(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Name.unsafe(rs.getString("name")),
        Description.unsafe(rs.getString("description")),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

package com.codeclash.authservice.session.adapter.outbound.jdbc;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.LastUsedAt;
import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class SessionJdbcRowMapper implements RowMapper<Session> {

  @Override
  public Session mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    return new Session(
        Id.unsafe(rs.getObject("id", UUID.class)),
        UserId.unsafe(rs.getObject("user_id", UUID.class)),
        new LastUsedAt(rs.getTimestamp("last_used_at").toInstant()),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

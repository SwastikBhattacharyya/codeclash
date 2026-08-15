package com.codeclash.problemservice.tag.adapter.outbound.jdbc;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Name;
import com.codeclash.problemservice.tag.model.Tag;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class TagJdbcRowMapper implements RowMapper<Tag> {

  @Override
  public Tag mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    return new Tag(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Name.unsafe(rs.getString("name")),
        Description.unsafe(rs.getString("description")),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

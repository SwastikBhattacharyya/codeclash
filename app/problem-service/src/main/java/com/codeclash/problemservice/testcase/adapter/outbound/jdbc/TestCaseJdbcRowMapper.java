package com.codeclash.problemservice.testcase.adapter.outbound.jdbc;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class TestCaseJdbcRowMapper implements RowMapper<TestCase> {

  @Override
  public TestCase mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    return new TestCase(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Input.unsafe(rs.getString("input")),
        Output.unsafe(rs.getString("output")),
        new Public(rs.getBoolean("is_public")),
        QuestionId.unsafe(rs.getObject("question_id", UUID.class)),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

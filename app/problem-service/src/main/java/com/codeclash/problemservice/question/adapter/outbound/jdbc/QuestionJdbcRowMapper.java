package com.codeclash.problemservice.question.adapter.outbound.jdbc;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.category.Categories;
import com.codeclash.problemservice.question.model.tag.Tags;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class QuestionJdbcRowMapper
    implements RowMapper<Question> {

  @Override
  public Question mapRow(@NonNull ResultSet rs, int rowNum)
      throws SQLException {
    UUID[] categoryIds = (UUID[]) rs.getArray("category_ids").getArray();
    UUID[] tagIds = (UUID[]) rs.getArray("tag_ids").getArray();
    return new Question(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Title.unsafe(rs.getString("title")),
        Summary.unsafe(rs.getString("summary")),
        Description.unsafe(rs.getString("description")),
        new Visible(rs.getBoolean("is_visible")),
        MaxRunTime.unsafe(rs.getInt("max_run_time")),
        MaxRamLimit.unsafe(rs.getInt("max_ram_limit")),
        Categories.unsafe(List.of(categoryIds)),
        Tags.unsafe(List.of(tagIds)),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

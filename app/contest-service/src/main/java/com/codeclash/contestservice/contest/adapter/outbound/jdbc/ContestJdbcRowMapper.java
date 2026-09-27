package com.codeclash.contestservice.contest.adapter.outbound.jdbc;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.EndAt;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.LiveAt;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.OpenedAt;
import com.codeclash.contestservice.contest.model.question.ContestQuestion;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ContestJdbcRowMapper implements RowMapper<Contest> {

  @Override
  public Contest mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    UUID[] questionIds = (UUID[]) rs.getArray("question_ids").getArray();
    Integer[] points = (Integer[]) rs.getArray("question_points").getArray();
    List<ContestQuestion> questions = new ArrayList<>(questionIds.length);
    for (int index = 0; index < questionIds.length; index++)
      questions.add(ContestQuestion.unsafe(questionIds[index], points[index]));
    return this.mapRow(rs, "", ContestQuestions.unsafe(questions));
  }

  public Contest mapRow(ResultSet rs, String prefix, ContestQuestions questions)
      throws SQLException {
    return new Contest(
        Id.unsafe(rs.getObject(prefix + "id", UUID.class)),
        Name.unsafe(rs.getString(prefix + "name")),
        Description.unsafe(rs.getString(prefix + "description")),
        new LiveAt(rs.getTimestamp(prefix + "live_at").toInstant()),
        new EndAt(rs.getTimestamp(prefix + "end_at").toInstant()),
        ContestJdbcRowMapper.openedAt(rs.getTimestamp(prefix + "opened_at")),
        questions,
        new CreatedAt(rs.getTimestamp(prefix + "created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp(prefix + "updated_at").toInstant()));
  }

  private static OpenedAt openedAt(Timestamp timestamp) {
    return timestamp == null ? null : new OpenedAt(timestamp.toInstant());
  }
}

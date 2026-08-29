package com.codeclash.problemservice.submission.adapter.outbound.jdbc;

import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.ActualOutput;
import com.codeclash.problemservice.submission.model.Code;
import com.codeclash.problemservice.submission.model.CompileOutput;
import com.codeclash.problemservice.submission.model.FailedTestCaseId;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.MemoryUsed;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.TimeTaken;
import com.codeclash.problemservice.submission.model.UserId;
import com.codeclash.problemservice.submission.model.Username;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class SubmissionJdbcRowMapper implements RowMapper<Submission> {

  @Override
  public Submission mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    UUID failedTestCaseId = rs.getObject("failed_test_case_id", UUID.class);
    Long timeTakenMs = rs.getObject("time_taken_ms", Long.class);
    Long memoryUsedKb = rs.getObject("memory_used_kb", Long.class);
    String compileOutput = rs.getString("compile_output");
    String actualOutput = rs.getString("actual_output");
    return new Submission(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Language.from(rs.getString("language")),
        Code.unsafe(rs.getString("code")),
        SubmissionState.from(rs.getString("state")),
        failedTestCaseId == null ? null : FailedTestCaseId.unsafe(failedTestCaseId),
        timeTakenMs == null ? null : new TimeTaken(timeTakenMs),
        memoryUsedKb == null ? null : new MemoryUsed(memoryUsedKb),
        compileOutput == null ? null : CompileOutput.unsafe(compileOutput),
        actualOutput == null ? null : ActualOutput.unsafe(actualOutput),
        QuestionId.unsafe(rs.getObject("question_id", UUID.class)),
        UserId.unsafe(rs.getObject("user_id", UUID.class)),
        Username.unsafe(rs.getString("username")),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

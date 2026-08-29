package com.codeclash.problemservice.submission.adapter.outbound.jdbc;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.UserId;
import com.codeclash.problemservice.submission.model.exception.ActualOutputTooLongException;
import com.codeclash.problemservice.submission.model.exception.BlankCodeException;
import com.codeclash.problemservice.submission.model.exception.CodeTooLongException;
import com.codeclash.problemservice.submission.model.exception.CompileOutputTooLongException;
import com.codeclash.problemservice.submission.model.exception.FailedTestCaseNotFoundException;
import com.codeclash.problemservice.submission.model.exception.InvalidFailedTestCaseIdException;
import com.codeclash.problemservice.submission.model.exception.InvalidLanguageException;
import com.codeclash.problemservice.submission.model.exception.InvalidSubmissionIdException;
import com.codeclash.problemservice.submission.model.exception.InvalidSubmissionResultException;
import com.codeclash.problemservice.submission.model.exception.InvalidSubmissionStateException;
import com.codeclash.problemservice.submission.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.submission.model.exception.UserNotFoundException;
import com.codeclash.problemservice.submission.model.owner.Owned;
import com.codeclash.problemservice.submission.model.request.ApplySubmissionStateRequest;
import com.codeclash.problemservice.submission.model.request.CreateSubmissionRequest;
import com.codeclash.problemservice.submission.model.request.QuerySubmissionRequest;
import com.codeclash.problemservice.submission.port.outbound.SubmissionPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Observed
@RequiredArgsConstructor
public class SubmissionJdbcPersistence implements SubmissionPersistence {

  private static final String CK_SUBMISSIONS_ID_VERSION = "ck_submissions_id_version";
  private static final String CK_SUBMISSIONS_LANGUAGE = "ck_submissions_language";
  private static final String CK_SUBMISSIONS_CODE_NOT_BLANK = "ck_submissions_code_not_blank";
  private static final String CK_SUBMISSIONS_CODE_MAX_SIZE = "ck_submissions_code_max_size";
  private static final String CK_SUBMISSIONS_STATE = "ck_submissions_state";
  private static final String FK_SUBMISSIONS_QUESTION = "fk_submissions_question";
  private static final String FK_SUBMISSIONS_USER = "fk_submissions_user";
  private static final String CK_SUBMISSIONS_RESULT_METRICS = "ck_submissions_result_metrics";
  private static final String CK_SUBMISSIONS_FAILED_TEST_CASE_STATE =
      "ck_submissions_failed_test_case_state";
  private static final String FK_SUBMISSIONS_FAILED_TEST_CASE = "fk_submissions_failed_test_case";
  private static final String CK_SUBMISSIONS_TIME_TAKEN_MS_NON_NEGATIVE =
      "ck_submissions_time_taken_ms_non_negative";
  private static final String CK_SUBMISSIONS_MEMORY_USED_KB_NON_NEGATIVE =
      "ck_submissions_memory_used_kb_non_negative";
  private static final String CK_SUBMISSIONS_FAILED_TEST_CASE_ID_VERSION =
      "ck_submissions_failed_test_case_id_version";
  private static final String CK_SUBMISSIONS_COMPILE_OUTPUT_MAX_SIZE =
      "ck_submissions_compile_output_max_size";
  private static final String CK_SUBMISSIONS_COMPILE_OUTPUT_STATE =
      "ck_submissions_compile_output_state";
  private static final String CK_SUBMISSIONS_ACTUAL_OUTPUT_MAX_SIZE =
      "ck_submissions_actual_output_max_size";
  private static final String CK_SUBMISSIONS_ACTUAL_OUTPUT_STATE =
      "ck_submissions_actual_output_state";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "language", "language",
          "state", "state",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final SubmissionJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<Submission> query(QuerySubmissionRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.in(
                "question_id", request.questionIds().stream().map(QuestionId::value).toList()),
            JdbcFilters.in("user_id", request.userIds().stream().map(UserId::value).toList()),
            SubmissionJdbcPersistence.usernames(request.usernames()),
            JdbcFilters.in("language", request.languages().stream().map(Language::value).toList()),
            JdbcFilters.in("state", request.states().stream().map(SubmissionState::value).toList()),
            SubmissionJdbcPersistence.questionVisible(request.questionVisible()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy =
        JdbcSorts.orderBy(request.sort(), SubmissionJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("submissions", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, language, code, state, failed_test_case_id, time_taken_ms, memory_used_kb, compile_output, actual_output,
          question_id, user_id,
  (select u.username from users u where u.id = submissions.user_id) as username,
  created_at, updated_at
        from submissions
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Submission> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  private static JdbcFilter usernames(List<String> usernames) {
    if (usernames.isEmpty()) return new JdbcFilter("", Map.of());
    return new JdbcFilter(
        "user_id in (select id from users where username in (:usernames))",
        Map.of("usernames", usernames));
  }

  private static JdbcFilter questionVisible(Boolean visible) {
    if (visible == null) return new JdbcFilter("", Map.of());
    return new JdbcFilter(
        "question_id in (select id from questions where is_visible = :question_visible)",
        Map.of("question_visible", visible));
  }

  @Override
  public Optional<Submission> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, language, code, state, failed_test_case_id, time_taken_ms, memory_used_kb, compile_output, actual_output, question_id, user_id,
  (select u.username from users u where u.id = submissions.user_id) as username,
  created_at, updated_at
from submissions
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Submission create(CreateSubmissionRequest<Owned> request, SubmissionState state) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into submissions(language, code, state, question_id, user_id)
values (:language, :code, :state, :questionId, :userId)
returning id, language, code, state, failed_test_case_id, time_taken_ms, memory_used_kb, compile_output, actual_output, question_id, user_id,
  (select u.username from users u where u.id = submissions.user_id) as username,
  created_at, updated_at
""")
          .param("language", request.language().value())
          .param("code", request.code().value())
          .param("state", state.value())
          .param("questionId", request.questionId().value())
          .param("userId", request.owner().userId().value())
          .query(this.rowMapper)
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception, this.constraintFinder, SubmissionJdbcPersistence.constraints(request, state));
    }
  }

  @Override
  public Optional<Submission> deleteById(Id id) {
    return this.jdbcClient
        .sql(
"""
delete from submissions where id = :id
returning id, language, code, state, failed_test_case_id, time_taken_ms, memory_used_kb, compile_output, actual_output, question_id, user_id,
  (select u.username from users u where u.id = submissions.user_id) as username,
  created_at, updated_at
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Optional<Submission> applyState(
      ApplySubmissionStateRequest request, List<SubmissionState> predecessors) {
    try {
      return this.jdbcClient
          .sql(
"""
update submissions set
  state = :state,
  failed_test_case_id = :failedTestCaseId,
  time_taken_ms = :timeTakenMs,
  memory_used_kb = :memoryUsedKb,
  compile_output = :compileOutput,
  actual_output = :actualOutput
where id = :id and state in (:predecessors)
returning id, language, code, state, failed_test_case_id, time_taken_ms, memory_used_kb, compile_output, actual_output,
  question_id, user_id,
  (select u.username from users u where u.id = submissions.user_id) as username,
  created_at, updated_at
""")
          .param("id", request.id().value())
          .param("state", request.state().value())
          .param(
              "failedTestCaseId",
              request.failedTestCaseId() == null ? null : request.failedTestCaseId().value())
          .param("timeTakenMs", request.timeTaken() == null ? null : request.timeTaken().value())
          .param(
              "memoryUsedKb", request.memoryUsed() == null ? null : request.memoryUsed().value())
          .param(
              "compileOutput",
              request.compileOutput() == null ? null : request.compileOutput().value())
          .param(
              "actualOutput",
              request.actualOutput() == null ? null : request.actualOutput().value())
          .param("predecessors", predecessors.stream().map(SubmissionState::value).toList())
          .query(this.rowMapper)
          .optional();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          Map.ofEntries(
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_STATE,
                  () -> new InvalidSubmissionStateException(request.state().value())),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_RESULT_METRICS,
                  () -> new InvalidSubmissionResultException(request.state().value())),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_FAILED_TEST_CASE_STATE,
                  () -> new InvalidSubmissionResultException(request.state().value())),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_TIME_TAKEN_MS_NON_NEGATIVE,
                  () -> new InvalidSubmissionResultException(request.state().value())),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_MEMORY_USED_KB_NON_NEGATIVE,
                  () -> new InvalidSubmissionResultException(request.state().value())),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_ACTUAL_OUTPUT_STATE,
                  () -> new InvalidSubmissionResultException(request.state().value())),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_ACTUAL_OUTPUT_MAX_SIZE,
                  ActualOutputTooLongException::new),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_COMPILE_OUTPUT_STATE,
                  () -> new InvalidSubmissionResultException(request.state().value())),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_COMPILE_OUTPUT_MAX_SIZE,
                  CompileOutputTooLongException::new),
              Map.entry(
                  SubmissionJdbcPersistence.CK_SUBMISSIONS_FAILED_TEST_CASE_ID_VERSION,
                  () -> new InvalidFailedTestCaseIdException(request.failedTestCaseId().value())),
              Map.entry(
                  SubmissionJdbcPersistence.FK_SUBMISSIONS_FAILED_TEST_CASE,
                  () -> new FailedTestCaseNotFoundException(request.failedTestCaseId().value()))));
    }
  }

  private static Map<String, Supplier<RuntimeException>> constraints(
      CreateSubmissionRequest<Owned> request, SubmissionState state) {
    return Map.ofEntries(
        Map.entry(
            SubmissionJdbcPersistence.CK_SUBMISSIONS_ID_VERSION,
            () -> new InvalidSubmissionIdException(null)),
        Map.entry(
            SubmissionJdbcPersistence.CK_SUBMISSIONS_LANGUAGE,
            () -> new InvalidLanguageException(request.language().value())),
        Map.entry(SubmissionJdbcPersistence.CK_SUBMISSIONS_CODE_NOT_BLANK, BlankCodeException::new),
        Map.entry(
            SubmissionJdbcPersistence.CK_SUBMISSIONS_CODE_MAX_SIZE, CodeTooLongException::new),
        Map.entry(
            SubmissionJdbcPersistence.CK_SUBMISSIONS_STATE,
            () -> new InvalidSubmissionStateException(state.value())),
        Map.entry(
            SubmissionJdbcPersistence.FK_SUBMISSIONS_QUESTION,
            () -> new QuestionNotFoundException(request.questionId().value())),
        Map.entry(
            SubmissionJdbcPersistence.FK_SUBMISSIONS_USER,
            () -> new UserNotFoundException(request.owner().userId().value())));
  }
}

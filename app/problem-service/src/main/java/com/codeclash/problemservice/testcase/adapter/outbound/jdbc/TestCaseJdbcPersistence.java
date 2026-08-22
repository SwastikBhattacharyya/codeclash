package com.codeclash.problemservice.testcase.adapter.outbound.jdbc;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.exception.InputTooLongException;
import com.codeclash.problemservice.testcase.model.exception.InvalidTestCaseIdException;
import com.codeclash.problemservice.testcase.model.exception.OutputTooLongException;
import com.codeclash.problemservice.testcase.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.testcase.model.request.CreateTestCaseRequest;
import com.codeclash.problemservice.testcase.model.request.PatchTestCaseRequest;
import com.codeclash.problemservice.testcase.model.request.QueryTestCaseRequest;
import com.codeclash.problemservice.testcase.model.request.ReplaceTestCaseRequest;
import com.codeclash.problemservice.testcase.model.result.ReplaceTestCaseResult;
import com.codeclash.problemservice.testcase.port.outbound.TestCasePersistence;
import io.micrometer.observation.annotation.Observed;
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
public class TestCaseJdbcPersistence implements TestCasePersistence {

  private static final String CK_TEST_CASES_ID_VERSION = "ck_test_cases_id_version";
  private static final String CK_TEST_CASES_INPUT_MAX_SIZE = "ck_test_cases_input_max_size";
  private static final String CK_TEST_CASES_OUTPUT_MAX_SIZE = "ck_test_cases_output_max_size";
  private static final String FK_TEST_CASES_QUESTION = "fk_test_cases_question";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "is_public", "is_public",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final TestCaseJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<TestCase> query(QueryTestCaseRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.equalTo("question_id", request.questionId().value()),
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.equalTo("is_public", request.isPublic()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy = JdbcSorts.orderBy(request.sort(), TestCaseJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("test_cases", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, input, output, is_public, question_id, created_at, updated_at
        from test_cases
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<TestCase> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<TestCase> findById(QuestionId questionId, Id id) {
    return this.jdbcClient
        .sql(
"""
select id, input, output, is_public, question_id, created_at, updated_at
from test_cases
where id = :id and question_id = :questionId
""")
        .param("id", id.value())
        .param("questionId", questionId.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public List<Id> findPublicIds(List<Id> ids) {
    return this.jdbcClient
        .sql("select id from test_cases where id in (:ids) and is_public")
        .param("ids", ids.stream().map(Id::value).toList())
        .query(UUID.class)
        .list()
        .stream()
        .map(Id::unsafe)
        .toList();
  }

  @Override
  public TestCase create(CreateTestCaseRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into test_cases(input, output, is_public, question_id)
values (:input, :output, :isPublic, :questionId)
returning id, input, output, is_public, question_id, created_at, updated_at
""")
          .param("input", request.input().value())
          .param("output", request.output().value())
          .param("isPublic", request.isPublic().value())
          .param("questionId", request.questionId().value())
          .query(this.rowMapper)
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          TestCaseJdbcPersistence.constraints(null, request.questionId().value()));
    }
  }

  @Override
  public Optional<ReplaceTestCaseResult> replace(ReplaceTestCaseRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into test_cases(id, input, output, is_public, question_id)
values (:id, :input, :output, :isPublic, :questionId)
on conflict (id) do update set
  input = excluded.input,
  output = excluded.output,
  is_public = excluded.is_public
where test_cases.question_id = excluded.question_id
returning id, input, output, is_public, question_id, created_at, updated_at, old.id is null as created
""")
          .param("id", request.id().value())
          .param("input", request.input().value())
          .param("output", request.output().value())
          .param("isPublic", request.isPublic().value())
          .param("questionId", request.questionId().value())
          .query((rs, rowNum) ->
              new ReplaceTestCaseResult(
                  this.rowMapper.mapRow(rs, rowNum), rs.getBoolean("created")))
          .optional();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          TestCaseJdbcPersistence.constraints(request.id().value(), request.questionId().value()));
    }
  }

  @Override
  public Optional<TestCase> patch(PatchTestCaseRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
update test_cases set
  input = coalesce(:input, input),
  output = coalesce(:output, output),
  is_public = coalesce(:isPublic, is_public)
where id = :id and question_id = :questionId
returning id, input, output, is_public, question_id, created_at, updated_at
""")
          .param("id", request.id().value())
          .param("questionId", request.questionId().value())
          .param("input", request.input().map(Input::value).orElse(null))
          .param("output", request.output().map(Output::value).orElse(null))
          .param("isPublic", request.isPublic().map(Public::value).orElse(null))
          .query(this.rowMapper)
          .optional();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          TestCaseJdbcPersistence.constraints(request.id().value(), request.questionId().value()));
    }
  }

  @Override
  public Optional<TestCase> deleteById(QuestionId questionId, Id id) {
    return this.jdbcClient
        .sql(
"""
delete from test_cases
where id = :id and question_id = :questionId
returning id, input, output, is_public, question_id, created_at, updated_at
""")
        .param("id", id.value())
        .param("questionId", questionId.value())
        .query(this.rowMapper)
        .optional();
  }

  private static Map<String, Supplier<RuntimeException>> constraints(UUID id, UUID questionId) {
    return Map.ofEntries(
        Map.entry(
            TestCaseJdbcPersistence.CK_TEST_CASES_ID_VERSION,
            () -> new InvalidTestCaseIdException(id)),
        Map.entry(TestCaseJdbcPersistence.CK_TEST_CASES_INPUT_MAX_SIZE, InputTooLongException::new),
        Map.entry(
            TestCaseJdbcPersistence.CK_TEST_CASES_OUTPUT_MAX_SIZE, OutputTooLongException::new),
        Map.entry(
            TestCaseJdbcPersistence.FK_TEST_CASES_QUESTION,
            () -> new QuestionNotFoundException(questionId)));
  }
}

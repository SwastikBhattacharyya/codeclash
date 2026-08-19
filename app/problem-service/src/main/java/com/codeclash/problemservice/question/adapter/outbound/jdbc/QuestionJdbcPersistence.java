package com.codeclash.problemservice.question.adapter.outbound.jdbc;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.category.Categories;
import com.codeclash.problemservice.question.model.category.CategoryId;
import com.codeclash.problemservice.question.model.exception.CategoriesNotFoundException;
import com.codeclash.problemservice.question.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.question.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.question.model.exception.InvalidDescriptionException;
import com.codeclash.problemservice.question.model.exception.InvalidQuestionIdException;
import com.codeclash.problemservice.question.model.exception.InvalidSummaryException;
import com.codeclash.problemservice.question.model.exception.InvalidTitleException;
import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRamLimitTooLowException;
import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooHighException;
import com.codeclash.problemservice.question.model.exception.MaxRunTimeTooLowException;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.model.exception.SummaryTooLongException;
import com.codeclash.problemservice.question.model.exception.SummaryTooShortException;
import com.codeclash.problemservice.question.model.exception.TagsNotFoundException;
import com.codeclash.problemservice.question.model.exception.TitleTooLongException;
import com.codeclash.problemservice.question.model.exception.TitleTooShortException;
import com.codeclash.problemservice.question.model.request.CreateQuestionRequest;
import com.codeclash.problemservice.question.model.request.PatchQuestionRequest;
import com.codeclash.problemservice.question.model.request.QueryQuestionRequest;
import com.codeclash.problemservice.question.model.request.ReplaceQuestionRequest;
import com.codeclash.problemservice.question.model.result.ReplaceQuestionResult;
import com.codeclash.problemservice.question.model.tag.TagId;
import com.codeclash.problemservice.question.model.tag.Tags;
import com.codeclash.problemservice.question.port.outbound.QuestionPersistence;
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
public class QuestionJdbcPersistence implements QuestionPersistence {

  private static final String CK_QUESTIONS_ID_VERSION = "ck_questions_id_version";
  private static final String CK_QUESTIONS_TITLE_MIN_SIZE = "ck_questions_title_min_size";
  private static final String CK_QUESTIONS_TITLE_MAX_SIZE = "ck_questions_title_max_size";
  private static final String CK_QUESTIONS_TITLE_SINGLE_LINE = "ck_questions_title_single_line";
  private static final String CK_QUESTIONS_TITLE_TRIMMED = "ck_questions_title_trimmed";
  private static final String CK_QUESTIONS_SUMMARY_MIN_SIZE = "ck_questions_summary_min_size";
  private static final String CK_QUESTIONS_SUMMARY_MAX_SIZE = "ck_questions_summary_max_size";
  private static final String CK_QUESTIONS_SUMMARY_TRIMMED = "ck_questions_summary_trimmed";
  private static final String CK_QUESTIONS_DESCRIPTION_MIN_SIZE =
      "ck_questions_description_min_size";
  private static final String CK_QUESTIONS_DESCRIPTION_MAX_SIZE =
      "ck_questions_description_max_size";
  private static final String CK_QUESTIONS_DESCRIPTION_TRIMMED =
      "ck_questions_description_trimmed";
  private static final String CK_QUESTIONS_MAX_RUN_TIME_MIN_SIZE =
      "ck_questions_max_run_time_min_size";
  private static final String CK_QUESTIONS_MAX_RUN_TIME_MAX_SIZE =
      "ck_questions_max_run_time_max_size";
  private static final String CK_QUESTIONS_MAX_RAM_LIMIT_MIN_SIZE =
      "ck_questions_max_ram_limit_min_size";
  private static final String CK_QUESTIONS_MAX_RAM_LIMIT_MAX_SIZE =
      "ck_questions_max_ram_limit_max_size";
  private static final String FK_QUESTION_CATEGORIES_CATEGORY = "fk_question_categories_category";
  private static final String FK_QUESTION_TAGS_TAG = "fk_question_tags_tag";
  private static final String FK_QUESTION_CATEGORIES_QUESTION = "fk_question_categories_question";
  private static final String FK_QUESTION_TAGS_QUESTION = "fk_question_tags_question";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "title", "lower(title)",
          "summary", "lower(summary)",
          "visible", "is_visible",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final QuestionJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<Question> query(
      QueryQuestionRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.containsAny("title", request.titles()),
            JdbcFilters.containsAny("summary", request.summaries()),
            JdbcFilters.containsAny("description", request.descriptions()),
            JdbcFilters.equalTo("is_visible", request.visible()),
            QuestionJdbcPersistence.hasAnyCategory(
                request.categoryIds().stream().map(CategoryId::value).toList()),
            QuestionJdbcPersistence.hasAnyTag(
                request.tagIds().stream().map(TagId::value).toList()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy = JdbcSorts.orderBy(request.sort(), QuestionJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("questions", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, title, summary, description, is_visible, max_run_time, max_ram_limit,
          array(select qc.category_id from question_categories qc
                where qc.question_id = questions.id order by qc.category_id) as category_ids,
          array(select qt.tag_id from question_tags qt
                where qt.question_id = questions.id order by qt.tag_id) as tag_ids,
          created_at, updated_at
        from questions
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Question> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<Question> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, title, summary, description, is_visible, max_run_time, max_ram_limit,
  array(select qc.category_id from question_categories qc where qc.question_id = questions.id
      order by qc.category_id) as category_ids,
  array(select qt.tag_id from question_tags qt where qt.question_id = questions.id
      order by qt.tag_id) as tag_ids,
  created_at, updated_at
from questions
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Question create(
      CreateQuestionRequest request) {
    List<UUID> categoryIds = QuestionJdbcPersistence.categoryIds(request.categories());
    List<UUID> tagIds = QuestionJdbcPersistence.tagIds(request.tags());
    try {
      Question question =
          this.jdbcClient
              .sql(
"""
insert into questions(title, summary, description, is_visible, max_run_time, max_ram_limit)
values (:title, :summary, :description, :visible, :maxRunTime, :maxRamLimit)
returning id, title, summary, description, is_visible, max_run_time, max_ram_limit,
  array(select qc.category_id from question_categories qc where qc.question_id = questions.id
      order by qc.category_id) as category_ids,
  array(select qt.tag_id from question_tags qt where qt.question_id = questions.id
      order by qt.tag_id) as tag_ids,
  created_at, updated_at
""")
              .param("title", request.title().value())
              .param("summary", request.summary().value())
              .param("description", request.description().value())
              .param("visible", request.visible().value())
              .param("maxRunTime", request.maxRunTime().value())
              .param("maxRamLimit", request.maxRamLimit().value())
              .query(this.rowMapper)
              .single();
      this.replaceCategories(question.id().value(), categoryIds);
      this.replaceTags(question.id().value(), tagIds);
      return question.withCategories(request.categories()).withTags(request.tags());
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          QuestionJdbcPersistence.constraints(
              null,
              request.title().value(),
              request.summary().value(),
              request.description().value(),
              request.maxRunTime().value(),
              request.maxRamLimit().value(),
              categoryIds,
              tagIds));
    }
  }

  @Override
  public ReplaceQuestionResult replace(
      ReplaceQuestionRequest request) {
    List<UUID> categoryIds = QuestionJdbcPersistence.categoryIds(request.categories());
    List<UUID> tagIds = QuestionJdbcPersistence.tagIds(request.tags());
    try {
      ReplaceQuestionResult result =
          this.jdbcClient
              .sql(
"""
insert into questions(id, title, summary, description, is_visible, max_run_time, max_ram_limit)
values (:id, :title, :summary, :description, :visible, :maxRunTime, :maxRamLimit)
on conflict (id) do update set
  title = excluded.title,
  summary = excluded.summary,
  description = excluded.description,
  is_visible = excluded.is_visible,
  max_run_time = excluded.max_run_time,
  max_ram_limit = excluded.max_ram_limit
returning id, title, summary, description, is_visible, max_run_time, max_ram_limit,
  array(select qc.category_id from question_categories qc where qc.question_id = questions.id
      order by qc.category_id) as category_ids,
  array(select qt.tag_id from question_tags qt where qt.question_id = questions.id
      order by qt.tag_id) as tag_ids,
  created_at, updated_at, old.id is null as created
""")
              .param("id", request.id().value())
              .param("title", request.title().value())
              .param("summary", request.summary().value())
              .param("description", request.description().value())
              .param("visible", request.visible().value())
              .param("maxRunTime", request.maxRunTime().value())
              .param("maxRamLimit", request.maxRamLimit().value())
              .query((rs, rowNum) ->
                  new ReplaceQuestionResult(
                      this.rowMapper.mapRow(rs, rowNum), rs.getBoolean("created")))
              .single();
      this.replaceCategories(request.id().value(), categoryIds);
      this.replaceTags(request.id().value(), tagIds);
      return new ReplaceQuestionResult(
          result.question().withCategories(request.categories()).withTags(request.tags()),
          result.created());
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          QuestionJdbcPersistence.constraints(
              request.id().value(),
              request.title().value(),
              request.summary().value(),
              request.description().value(),
              request.maxRunTime().value(),
              request.maxRamLimit().value(),
              categoryIds,
              tagIds));
    }
  }

  @Override
  public Optional<Question> patch(
      PatchQuestionRequest request) {
    String title = request.title().map(Title::value).orElse(null);
    String summary = request.summary().map(Summary::value).orElse(null);
    String description = request.description().map(Description::value).orElse(null);
    Integer maxRunTime = request.maxRunTime().map(MaxRunTime::value).orElse(null);
    Integer maxRamLimit = request.maxRamLimit().map(MaxRamLimit::value).orElse(null);
    List<UUID> categoryIds =
        request.categories().map(QuestionJdbcPersistence::categoryIds).orElse(List.of());
    List<UUID> tagIds = request.tags().map(QuestionJdbcPersistence::tagIds).orElse(List.of());
    try {
      Optional<Question> patched =
          this.jdbcClient
              .sql(
"""
update questions set
  title = coalesce(:title, title),
  summary = coalesce(:summary, summary),
  description = coalesce(:description, description),
  is_visible = coalesce(:visible, is_visible),
  max_run_time = coalesce(:maxRunTime, max_run_time),
  max_ram_limit = coalesce(:maxRamLimit, max_ram_limit)
where id = :id
returning id, title, summary, description, is_visible, max_run_time, max_ram_limit,
  array(select qc.category_id from question_categories qc where qc.question_id = questions.id
      order by qc.category_id) as category_ids,
  array(select qt.tag_id from question_tags qt where qt.question_id = questions.id
      order by qt.tag_id) as tag_ids,
  created_at, updated_at
""")
              .param("id", request.id().value())
              .param("title", title)
              .param("summary", summary)
              .param("description", description)
              .param("visible", request.visible().map(Visible::value).orElse(null))
              .param("maxRunTime", maxRunTime)
              .param("maxRamLimit", maxRamLimit)
              .query(this.rowMapper)
              .optional();
      if (patched.isEmpty()) return patched;
      Question question = patched.get();
      if (request.categories().isPresent()) {
        this.replaceCategories(request.id().value(), categoryIds);
        question = question.withCategories(request.categories().orElse(null));
      }
      if (request.tags().isPresent()) {
        this.replaceTags(request.id().value(), tagIds);
        question = question.withTags(request.tags().orElse(null));
      }
      return Optional.of(question);
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          QuestionJdbcPersistence.constraints(
              request.id().value(),
              title,
              summary,
              description,
              maxRunTime,
              maxRamLimit,
              categoryIds,
              tagIds));
    }
  }

  @Override
  public Optional<Question> deleteById(Id id) {
    return this.jdbcClient
        .sql(
"""
delete from questions where id = :id
returning id, title, summary, description, is_visible, max_run_time, max_ram_limit,
  array(select qc.category_id from question_categories qc where qc.question_id = questions.id
      order by qc.category_id) as category_ids,
  array(select qt.tag_id from question_tags qt where qt.question_id = questions.id
      order by qt.tag_id) as tag_ids,
  created_at, updated_at
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  private void replaceCategories(UUID questionId, List<UUID> categoryIds) {
    this.jdbcClient
        .sql(
"""
delete from question_categories where question_id = :questionId
""")
        .param("questionId", questionId)
        .update();

    if (categoryIds.isEmpty()) return;

    this.jdbcClient
        .sql(
"""
insert into question_categories(question_id, category_id)
select :questionId, category_id from unnest(array[:categoryIds]::uuid[]) as category_id
""")
        .param("questionId", questionId)
        .param("categoryIds", categoryIds)
        .update();
  }

  private void replaceTags(UUID questionId, List<UUID> tagIds) {
    this.jdbcClient
        .sql(
"""
delete from question_tags where question_id = :questionId
""")
        .param("questionId", questionId)
        .update();

    if (tagIds.isEmpty()) return;

    this.jdbcClient
        .sql(
"""
insert into question_tags(question_id, tag_id)
select :questionId, tag_id from unnest(array[:tagIds]::uuid[]) as tag_id
""")
        .param("questionId", questionId)
        .param("tagIds", tagIds)
        .update();
  }

  private static List<UUID> categoryIds(Categories categories) {
    return categories.ids().stream().map(CategoryId::value).toList();
  }

  private static List<UUID> tagIds(Tags tags) {
    return tags.ids().stream().map(TagId::value).toList();
  }

  private static JdbcFilter hasAnyCategory(List<UUID> categoryIds) {
    if (categoryIds == null || categoryIds.isEmpty()) return JdbcFilters.and();
    return new JdbcFilter(
        "exists (select 1 from question_categories qc where qc.question_id = questions.id"
            + " and qc.category_id in (:category_ids))",
        Map.of("category_ids", categoryIds));
  }

  private static JdbcFilter hasAnyTag(List<UUID> tagIds) {
    if (tagIds == null || tagIds.isEmpty()) return JdbcFilters.and();
    return new JdbcFilter(
        "exists (select 1 from question_tags qt where qt.question_id = questions.id"
            + " and qt.tag_id in (:tag_ids))",
        Map.of("tag_ids", tagIds));
  }

  private static Map<String, Supplier<RuntimeException>> constraints(
      UUID id,
      String title,
      String summary,
      String description,
      Integer maxRunTime,
      Integer maxRamLimit,
      List<UUID> categoryIds,
      List<UUID> tagIds) {
    return Map.ofEntries(
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_ID_VERSION,
            () -> new InvalidQuestionIdException(id)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_TITLE_MIN_SIZE,
            () -> new TitleTooShortException(title)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_TITLE_MAX_SIZE,
            () -> new TitleTooLongException(title)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_TITLE_SINGLE_LINE,
            () -> new InvalidTitleException(title)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_TITLE_TRIMMED,
            () -> new InvalidTitleException(title)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_SUMMARY_MIN_SIZE,
            () -> new SummaryTooShortException(summary)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_SUMMARY_MAX_SIZE,
            () -> new SummaryTooLongException(summary)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_SUMMARY_TRIMMED,
            () -> new InvalidSummaryException(summary)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_DESCRIPTION_MIN_SIZE,
            () -> new DescriptionTooShortException(description)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_DESCRIPTION_MAX_SIZE,
            () -> new DescriptionTooLongException(description)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_DESCRIPTION_TRIMMED,
            () -> new InvalidDescriptionException(description)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_MAX_RUN_TIME_MIN_SIZE,
            () -> new MaxRunTimeTooLowException(maxRunTime)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_MAX_RUN_TIME_MAX_SIZE,
            () -> new MaxRunTimeTooHighException(maxRunTime)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_MAX_RAM_LIMIT_MIN_SIZE,
            () -> new MaxRamLimitTooLowException(maxRamLimit)),
        Map.entry(
            QuestionJdbcPersistence.CK_QUESTIONS_MAX_RAM_LIMIT_MAX_SIZE,
            () -> new MaxRamLimitTooHighException(maxRamLimit)),
        Map.entry(
            QuestionJdbcPersistence.FK_QUESTION_CATEGORIES_CATEGORY,
            () -> new CategoriesNotFoundException(categoryIds)),
        Map.entry(
            QuestionJdbcPersistence.FK_QUESTION_TAGS_TAG, () -> new TagsNotFoundException(tagIds)),
        Map.entry(
            QuestionJdbcPersistence.FK_QUESTION_CATEGORIES_QUESTION,
            () -> new QuestionNotFoundException(id)),
        Map.entry(
            QuestionJdbcPersistence.FK_QUESTION_TAGS_QUESTION,
            () -> new QuestionNotFoundException(id)));
  }
}

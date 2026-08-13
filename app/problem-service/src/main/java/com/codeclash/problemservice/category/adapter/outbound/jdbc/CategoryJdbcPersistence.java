package com.codeclash.problemservice.category.adapter.outbound.jdbc;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import com.codeclash.problemservice.category.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.category.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.category.model.exception.DuplicateCategoryNameException;
import com.codeclash.problemservice.category.model.exception.InvalidCategoryIdException;
import com.codeclash.problemservice.category.model.exception.InvalidDescriptionException;
import com.codeclash.problemservice.category.model.exception.InvalidNameException;
import com.codeclash.problemservice.category.model.exception.NameTooLongException;
import com.codeclash.problemservice.category.model.exception.NameTooShortException;
import com.codeclash.problemservice.category.model.request.CreateCategoryRequest;
import com.codeclash.problemservice.category.model.request.PatchCategoryRequest;
import com.codeclash.problemservice.category.model.request.QueryCategoryRequest;
import com.codeclash.problemservice.category.model.request.ReplaceCategoryRequest;
import com.codeclash.problemservice.category.model.result.ReplaceCategoryResult;
import com.codeclash.problemservice.category.port.outbound.CategoryPersistence;
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
public class CategoryJdbcPersistence implements CategoryPersistence {

  private static final String UK_CATEGORIES_NAME = "uk_categories_name";
  private static final String CK_CATEGORIES_ID_VERSION = "ck_categories_id_version";
  private static final String CK_CATEGORIES_NAME_MIN_SIZE = "ck_categories_name_min_size";
  private static final String CK_CATEGORIES_NAME_MAX_SIZE = "ck_categories_name_max_size";
  private static final String CK_CATEGORIES_NAME_SINGLE_LINE = "ck_categories_name_single_line";
  private static final String CK_CATEGORIES_NAME_TRIMMED = "ck_categories_name_trimmed";
  private static final String CK_CATEGORIES_DESCRIPTION_MIN_SIZE =
      "ck_categories_description_min_size";
  private static final String CK_CATEGORIES_DESCRIPTION_MAX_SIZE =
      "ck_categories_description_max_size";
  private static final String CK_CATEGORIES_DESCRIPTION_TRIMMED =
      "ck_categories_description_trimmed";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "name", "lower(name)",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final CategoryJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<Category> query(QueryCategoryRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.containsAny("name", request.names()),
            JdbcFilters.containsAny("description", request.descriptions()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy = JdbcSorts.orderBy(request.sort(), CategoryJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("categories", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, name, description, created_at, updated_at
        from categories
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Category> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<Category> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, created_at, updated_at
from categories
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Category create(CreateCategoryRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into categories(name, description)
values (:name, :description)
returning id, name, description, created_at, updated_at
""")
          .param("name", request.name().value())
          .param("description", request.description().value())
          .query(this.rowMapper)
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          CategoryJdbcPersistence.constraints(
              null, request.name().value(), request.description().value()));
    }
  }

  @Override
  public ReplaceCategoryResult replace(ReplaceCategoryRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into categories(id, name, description)
values (:id, :name, :description)
on conflict (id) do update set
  name = excluded.name,
  description = excluded.description
returning id, name, description, created_at, updated_at, old.id is null as created
""")
          .param("id", request.id().value())
          .param("name", request.name().value())
          .param("description", request.description().value())
          .query((rs, rowNum) ->
              new ReplaceCategoryResult(
                  this.rowMapper.mapRow(rs, rowNum), rs.getBoolean("created")))
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          CategoryJdbcPersistence.constraints(
              request.id().value(), request.name().value(), request.description().value()));
    }
  }

  @Override
  public Optional<Category> patch(PatchCategoryRequest request) {
    String name = request.name().map(Name::value).orElse(null);
    String description = request.description().map(Description::value).orElse(null);
    try {
      return this.jdbcClient
          .sql(
"""
update categories set
  name = coalesce(:name, name),
  description = coalesce(:description, description)
where id = :id
returning id, name, description, created_at, updated_at
""")
          .param("id", request.id().value())
          .param("name", name)
          .param("description", description)
          .query(this.rowMapper)
          .optional();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          CategoryJdbcPersistence.constraints(request.id().value(), name, description));
    }
  }

  @Override
  public boolean deleteById(Id id) {
    return this.jdbcClient
            .sql(
"""
delete from categories where id = :id
""")
            .param("id", id.value())
            .update()
        > 0;
  }

  private static Map<String, Supplier<RuntimeException>> constraints(
      UUID id, String name, String description) {
    return Map.ofEntries(
        Map.entry(
            CategoryJdbcPersistence.UK_CATEGORIES_NAME,
            () -> new DuplicateCategoryNameException(name)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_ID_VERSION,
            () -> new InvalidCategoryIdException(id)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_NAME_MIN_SIZE,
            () -> new NameTooShortException(name)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_NAME_MAX_SIZE,
            () -> new NameTooLongException(name)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_NAME_SINGLE_LINE,
            () -> new InvalidNameException(name)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_NAME_TRIMMED,
            () -> new InvalidNameException(name)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_DESCRIPTION_MIN_SIZE,
            () -> new DescriptionTooShortException(description)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_DESCRIPTION_MAX_SIZE,
            () -> new DescriptionTooLongException(description)),
        Map.entry(
            CategoryJdbcPersistence.CK_CATEGORIES_DESCRIPTION_TRIMMED,
            () -> new InvalidDescriptionException(description)));
  }
}

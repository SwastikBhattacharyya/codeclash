package com.codeclash.problemservice.tag.adapter.outbound.jdbc;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import com.codeclash.common.exception.jdbc.JdbcConstraints;
import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.filter.jdbc.JdbcFilters;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.jdbc.JdbcPage;
import com.codeclash.common.page.jdbc.JdbcPages;
import com.codeclash.common.sort.jdbc.JdbcSorts;
import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Name;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.exception.DescriptionTooLongException;
import com.codeclash.problemservice.tag.model.exception.DescriptionTooShortException;
import com.codeclash.problemservice.tag.model.exception.DuplicateTagNameException;
import com.codeclash.problemservice.tag.model.exception.InvalidDescriptionException;
import com.codeclash.problemservice.tag.model.exception.InvalidNameException;
import com.codeclash.problemservice.tag.model.exception.InvalidTagIdException;
import com.codeclash.problemservice.tag.model.exception.NameTooLongException;
import com.codeclash.problemservice.tag.model.exception.NameTooShortException;
import com.codeclash.problemservice.tag.model.request.CreateTagRequest;
import com.codeclash.problemservice.tag.model.request.PatchTagRequest;
import com.codeclash.problemservice.tag.model.request.QueryTagRequest;
import com.codeclash.problemservice.tag.model.request.ReplaceTagRequest;
import com.codeclash.problemservice.tag.model.result.ReplaceTagResult;
import com.codeclash.problemservice.tag.port.outbound.TagPersistence;
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
public class TagJdbcPersistence implements TagPersistence {

  private static final String UK_TAGS_NAME = "uk_tags_name";
  private static final String CK_TAGS_ID_VERSION = "ck_tags_id_version";
  private static final String CK_TAGS_NAME_MIN_SIZE = "ck_tags_name_min_size";
  private static final String CK_TAGS_NAME_MAX_SIZE = "ck_tags_name_max_size";
  private static final String CK_TAGS_NAME_SINGLE_LINE = "ck_tags_name_single_line";
  private static final String CK_TAGS_NAME_TRIMMED = "ck_tags_name_trimmed";
  private static final String CK_TAGS_DESCRIPTION_MIN_SIZE = "ck_tags_description_min_size";
  private static final String CK_TAGS_DESCRIPTION_MAX_SIZE = "ck_tags_description_max_size";
  private static final String CK_TAGS_DESCRIPTION_TRIMMED = "ck_tags_description_trimmed";

  private static final Map<String, String> SORT_COLUMNS =
      Map.of(
          "id", "id",
          "name", "lower(name)",
          "created_at", "created_at",
          "updated_at", "updated_at");

  private final JdbcClient jdbcClient;
  private final TagJdbcRowMapper rowMapper;
  private final ConstraintFinder constraintFinder;

  @Override
  public PagedEntity<Tag> query(QueryTagRequest request) {
    JdbcFilter filter =
        JdbcFilters.and(
            JdbcFilters.in("id", request.ids().stream().map(Id::value).toList()),
            JdbcFilters.containsAny("name", request.names()),
            JdbcFilters.containsAny("description", request.descriptions()),
            JdbcFilters.between("created_at", request.minCreatedAt(), request.maxCreatedAt()),
            JdbcFilters.between("updated_at", request.minUpdatedAt(), request.maxUpdatedAt()));
    JdbcPage page = JdbcPages.limitOffset(request.pagination());
    String orderBy = JdbcSorts.orderBy(request.sort(), TagJdbcPersistence.SORT_COLUMNS, "id");

    String countSql = JdbcPages.count("tags", filter);
    int items = this.jdbcClient.sql(countSql).params(filter.params()).query(Integer.class).single();

    String sql =
        """
        select id, name, description, created_at, updated_at
        from tags
        """
            + filter.whereClause()
            + orderBy
            + page.sql();

    List<Tag> data =
        this.jdbcClient
            .sql(sql)
            .params(filter.params())
            .params(page.params())
            .query(this.rowMapper)
            .list();

    return JdbcPages.toPagedEntity(data, items, request.pagination());
  }

  @Override
  public Optional<Tag> findById(Id id) {
    return this.jdbcClient
        .sql(
"""
select id, name, description, created_at, updated_at
from tags
where id = :id
""")
        .param("id", id.value())
        .query(this.rowMapper)
        .optional();
  }

  @Override
  public Tag create(CreateTagRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into tags(name, description)
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
          TagJdbcPersistence.constraints(
              null, request.name().value(), request.description().value()));
    }
  }

  @Override
  public ReplaceTagResult replace(ReplaceTagRequest request) {
    try {
      return this.jdbcClient
          .sql(
"""
insert into tags(id, name, description)
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
              new ReplaceTagResult(this.rowMapper.mapRow(rs, rowNum), rs.getBoolean("created")))
          .single();
    } catch (DataIntegrityViolationException exception) {
      throw JdbcConstraints.translate(
          exception,
          this.constraintFinder,
          TagJdbcPersistence.constraints(
              request.id().value(), request.name().value(), request.description().value()));
    }
  }

  @Override
  public Optional<Tag> patch(PatchTagRequest request) {
    String name = request.name().map(Name::value).orElse(null);
    String description = request.description().map(Description::value).orElse(null);
    try {
      return this.jdbcClient
          .sql(
"""
update tags set
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
          TagJdbcPersistence.constraints(request.id().value(), name, description));
    }
  }

  @Override
  public boolean deleteById(Id id) {
    return this.jdbcClient
            .sql(
"""
delete from tags where id = :id
""")
            .param("id", id.value())
            .update()
        > 0;
  }

  private static Map<String, Supplier<RuntimeException>> constraints(
      UUID id, String name, String description) {
    return Map.ofEntries(
        Map.entry(TagJdbcPersistence.UK_TAGS_NAME, () -> new DuplicateTagNameException(name)),
        Map.entry(TagJdbcPersistence.CK_TAGS_ID_VERSION, () -> new InvalidTagIdException(id)),
        Map.entry(TagJdbcPersistence.CK_TAGS_NAME_MIN_SIZE, () -> new NameTooShortException(name)),
        Map.entry(TagJdbcPersistence.CK_TAGS_NAME_MAX_SIZE, () -> new NameTooLongException(name)),
        Map.entry(
            TagJdbcPersistence.CK_TAGS_NAME_SINGLE_LINE, () -> new InvalidNameException(name)),
        Map.entry(TagJdbcPersistence.CK_TAGS_NAME_TRIMMED, () -> new InvalidNameException(name)),
        Map.entry(
            TagJdbcPersistence.CK_TAGS_DESCRIPTION_MIN_SIZE,
            () -> new DescriptionTooShortException(description)),
        Map.entry(
            TagJdbcPersistence.CK_TAGS_DESCRIPTION_MAX_SIZE,
            () -> new DescriptionTooLongException(description)),
        Map.entry(
            TagJdbcPersistence.CK_TAGS_DESCRIPTION_TRIMMED,
            () -> new InvalidDescriptionException(description)));
  }
}

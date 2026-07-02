package com.codeclash.common.filter.jdbc;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JdbcFilters {

  private static final JdbcFilter EMPTY = new JdbcFilter("", Map.of());

  private JdbcFilters() {}

  public static JdbcFilter in(String column, Collection<?> values) {
    if (values == null || values.isEmpty()) return JdbcFilters.EMPTY;
    return new JdbcFilter(column + " in (:" + column + ")", Map.of(column, values));
  }

  public static JdbcFilter containsAny(String column, List<String> terms) {
    if (terms == null) return JdbcFilters.EMPTY;
    List<String> patterns =
        terms.stream()
            .filter(term -> term != null && !term.isBlank())
            .map(JdbcFilters::containsPattern)
            .toList();
    if (patterns.isEmpty()) return JdbcFilters.EMPTY;

    List<String> clauses = new ArrayList<>();
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < patterns.size(); i++) {
      String param = column + "_" + i;
      clauses.add(column + " ilike :" + param);
      params.put(param, patterns.get(i));
    }
    return new JdbcFilter("(" + String.join(" or ", clauses) + ")", Map.copyOf(params));
  }

  public static JdbcFilter equalTo(String column, Object value) {
    if (value == null) return JdbcFilters.EMPTY;
    return new JdbcFilter(column + " = :" + column, Map.of(column, value));
  }

  public static JdbcFilter between(String column, Number min, Number max) {
    return JdbcFilters.range(column, min, max);
  }

  public static JdbcFilter between(String column, Instant min, Instant max) {
    return JdbcFilters.range(
        column, JdbcFilters.toOffsetDateTime(min), JdbcFilters.toOffsetDateTime(max));
  }

  public static JdbcFilter and(JdbcFilter... filters) {
    List<JdbcFilter> present =
        Arrays.stream(filters).filter(filter -> !filter.isEmpty()).toList();
    if (present.isEmpty()) return JdbcFilters.EMPTY;

    String sql = present.stream().map(JdbcFilter::sql).collect(Collectors.joining(" and "));
    Map<String, Object> params = new HashMap<>();
    present.forEach(filter -> params.putAll(filter.params()));
    return new JdbcFilter(sql, Map.copyOf(params));
  }

  private static JdbcFilter range(String column, Object min, Object max) {
    List<String> clauses = new ArrayList<>();
    Map<String, Object> params = new HashMap<>();
    if (min != null) {
      clauses.add(column + " >= :" + column + "_min");
      params.put(column + "_min", min);
    }
    if (max != null) {
      clauses.add(column + " <= :" + column + "_max");
      params.put(column + "_max", max);
    }
    if (clauses.isEmpty()) return JdbcFilters.EMPTY;
    return new JdbcFilter(String.join(" and ", clauses), Map.copyOf(params));
  }

  private static String containsPattern(String term) {
    String escaped = term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    return "%" + escaped + "%";
  }

  private static OffsetDateTime toOffsetDateTime(Instant instant) {
    return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
  }
}

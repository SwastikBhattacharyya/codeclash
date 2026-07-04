package com.codeclash.common.sort.jdbc;

import com.codeclash.common.sort.SortRequest;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import java.util.Locale;
import java.util.Map;

public final class JdbcSorts {

  private JdbcSorts() {}

  public static String orderBy(SortRequest sort, Map<String, String> columns, String tieBreaker) {
    String column = sort.field() == null ? null : columns.get(sort.field());
    if (column == null)
      throw new UnknownSortFieldException(
          sort.field(), columns.keySet().stream().sorted().toList());

    String orderBy = "order by " + column + " " + sort.direction().name().toLowerCase(Locale.ROOT);
    if (column.equals(tieBreaker)) return orderBy + "\n";
    return orderBy + ", " + tieBreaker + " asc\n";
  }
}

package com.codeclash.common.page.jdbc;

import com.codeclash.common.filter.jdbc.JdbcFilter;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import java.util.List;
import java.util.Map;

public final class JdbcPages {

  private JdbcPages() {}

  public static JdbcPage limitOffset(PaginationRequest pagination) {
    return new JdbcPage(
        "limit :page_limit offset :page_offset\n",
        Map.of(
            "page_limit",
            pagination.size().value(),
            "page_offset",
            JdbcPages.offset(pagination)));
  }

  public static String count(String table, JdbcFilter filter) {
    return "select count(*)\nfrom " + table + "\n" + filter.whereClause();
  }

  public static <T> PagedEntity<T> toPagedEntity(
      List<T> data, int items, PaginationRequest pagination) {
    return new PagedEntity<>(
        data,
        pagination.page().value(),
        pagination.size().value(),
        items,
        JdbcPages.offset(pagination) + data.size() < items);
  }

  private static long offset(PaginationRequest pagination) {
    return (pagination.page().value() - 1L) * pagination.size().value();
  }
}

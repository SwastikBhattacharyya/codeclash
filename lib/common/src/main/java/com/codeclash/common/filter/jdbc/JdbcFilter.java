package com.codeclash.common.filter.jdbc;

import java.util.Map;

public record JdbcFilter(String sql, Map<String, Object> params) {

  public boolean isEmpty() {
    return this.sql.isEmpty();
  }

  public String whereClause() {
    return this.isEmpty() ? "" : "where " + this.sql + "\n";
  }
}

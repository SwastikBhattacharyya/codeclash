package com.codeclash.common.page.jdbc;

import java.util.Map;

public record JdbcPage(String sql, Map<String, Object> params) {}

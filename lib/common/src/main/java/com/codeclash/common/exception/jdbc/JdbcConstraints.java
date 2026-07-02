package com.codeclash.common.exception.jdbc;

import java.util.Map;
import java.util.function.Supplier;
import org.springframework.dao.DataIntegrityViolationException;

public final class JdbcConstraints {

  private JdbcConstraints() {}

  public static RuntimeException translate(
      DataIntegrityViolationException exception,
      ConstraintFinder finder,
      Map<String, Supplier<RuntimeException>> exceptions) {
    return finder
        .find(exception)
        .map(exceptions::get)
        .map(Supplier::get)
        .orElse(exception);
  }
}

package com.codeclash.common.exception.jdbc;

import com.codeclash.common.exception.jdbc.exception.UnsupportedDatabaseException;
import com.codeclash.common.exception.jdbc.postgres.PostgresConstraintFinder;
import org.springframework.boot.jdbc.DatabaseDriver;

public final class ConstraintFinders {

  private ConstraintFinders() {}

  public static ConstraintFinder forDatabase(DatabaseDriver driver) {
    return switch (driver) {
      case POSTGRESQL -> new PostgresConstraintFinder();
      default -> throw new UnsupportedDatabaseException(driver.name());
    };
  }
}

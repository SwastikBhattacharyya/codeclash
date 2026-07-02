package com.codeclash.common.exception.jdbc.postgres;

import com.codeclash.common.exception.jdbc.ConstraintFinder;
import java.util.Optional;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

public class PostgresConstraintFinder implements ConstraintFinder {

  @Override
  public Optional<String> find(Throwable exception) {
    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause instanceof PSQLException postgres) {
        return Optional.ofNullable(postgres.getServerErrorMessage())
            .map(ServerErrorMessage::getConstraint);
      }
    }
    return Optional.empty();
  }
}

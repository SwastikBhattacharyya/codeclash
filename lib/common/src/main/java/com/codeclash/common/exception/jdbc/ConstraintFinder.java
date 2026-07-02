package com.codeclash.common.exception.jdbc;

import java.util.Optional;

public interface ConstraintFinder {

  Optional<String> find(Throwable exception);
}

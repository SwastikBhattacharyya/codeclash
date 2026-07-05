package com.codeclash.common.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;

final class ParentlessObservationPredicate implements ObservationPredicate {

  private static final String ANNOTATED_METHOD = "method.observed";
  private static final String JDBC_PREFIX = "jdbc.";

  private static boolean droppedWhenParentless(String name) {
    return ParentlessObservationPredicate.ANNOTATED_METHOD.equals(name)
        || name.startsWith(ParentlessObservationPredicate.JDBC_PREFIX);
  }

  @Override
  public boolean test(String name, Observation.Context context) {
    return context.getParentObservation() != null
        || !ParentlessObservationPredicate.droppedWhenParentless(name);
  }
}

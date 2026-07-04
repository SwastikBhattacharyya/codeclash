package com.codeclash.common.sort;

import com.codeclash.common.sort.exception.InvalidSortDirectionException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum Direction {
  ASC,
  DESC;

  public static Direction from(String value) {
    for (Direction direction : Direction.values()) {
      if (direction.name().equalsIgnoreCase(value)) return direction;
    }
    throw new InvalidSortDirectionException(value);
  }

  public static List<String> allowed() {
    return Arrays.stream(Direction.values())
        .map(value -> value.name().toLowerCase(Locale.ROOT))
        .toList();
  }
}

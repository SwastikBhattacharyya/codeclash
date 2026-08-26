package com.codeclash.contracts.problem.submission;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum Language {
  CPP("cpp"),
  PYTHON("python"),
  JAVA("java");

  private final String value;

  Language(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return this.value;
  }

  public static Optional<Language> find(String value) {
    return Arrays.stream(Language.values())
        .filter(language -> language.value.equals(value))
        .findFirst();
  }

  @JsonCreator
  public static Language from(String value) {
    return Language.find(value)
        .orElseThrow(() -> new IllegalArgumentException("Unknown Language: %s".formatted(value)));
  }

  public static List<String> allowed() {
    return Arrays.stream(Language.values()).map(Language::value).toList();
  }
}

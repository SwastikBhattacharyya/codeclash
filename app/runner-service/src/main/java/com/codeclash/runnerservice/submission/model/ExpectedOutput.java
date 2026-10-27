package com.codeclash.runnerservice.submission.model;

import java.util.ArrayList;
import java.util.List;

public record ExpectedOutput(String value) {

  private static final String LINE_SEPARATOR = "\n";

  private static List<String> normalize(String text) {
    List<String> lines = new ArrayList<>();
    for (String line : text.split(ExpectedOutput.LINE_SEPARATOR, -1)) lines.add(line.stripTrailing());
    while (!lines.isEmpty() && lines.getLast().isEmpty()) lines.removeLast();
    return lines;
  }

  public boolean matches(String actual) {
    return actual != null
        && ExpectedOutput.normalize(this.value).equals(ExpectedOutput.normalize(actual));
  }
}

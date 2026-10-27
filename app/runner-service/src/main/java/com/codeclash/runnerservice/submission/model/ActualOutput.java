package com.codeclash.runnerservice.submission.model;

public record ActualOutput(String value) {

  public static final int MAX_SIZE = 65_536;
  public static final String TRUNCATION_MARKER = "\n\u2026 output truncated\n";
  private static final String NUL = "\u0000";

  public static ActualOutput of(String value) {
    String output = value == null ? "" : value.replace(ActualOutput.NUL, "");
    if (output.codePointCount(0, output.length()) <= ActualOutput.MAX_SIZE)
      return new ActualOutput(output);
    int kept =
        ActualOutput.MAX_SIZE
            - ActualOutput.TRUNCATION_MARKER.codePointCount(
                0, ActualOutput.TRUNCATION_MARKER.length());
    return new ActualOutput(
        output.substring(0, output.offsetByCodePoints(0, kept)) + ActualOutput.TRUNCATION_MARKER);
  }
}

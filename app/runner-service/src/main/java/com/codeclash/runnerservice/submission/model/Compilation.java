package com.codeclash.runnerservice.submission.model;

public record Compilation(boolean compiled, String output) {

  public static Compilation success() {
    return new Compilation(true, null);
  }

  public static Compilation failure(String output) {
    return new Compilation(false, output);
  }
}

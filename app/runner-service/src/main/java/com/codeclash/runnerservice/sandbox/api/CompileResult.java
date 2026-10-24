package com.codeclash.runnerservice.sandbox.api;

public record CompileResult(CompileOutcome outcome, String output) {

  public static CompileResult compiled() {
    return new CompileResult(CompileOutcome.COMPILED, null);
  }

  public static CompileResult compilationError(String output) {
    return new CompileResult(CompileOutcome.COMPILATION_ERROR, output);
  }
}

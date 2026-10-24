package com.codeclash.runnerservice.sandbox.api;

import com.codeclash.contracts.problem.submission.Language;

public interface Sandbox extends AutoCloseable {

  void writeSource(Language language, String code);

  CompileResult compile(Language language);

  void limitMemory(int megabytes);

  RunResult run(Language language, int timeLimitMs, String input);

  @Override
  void close();
}

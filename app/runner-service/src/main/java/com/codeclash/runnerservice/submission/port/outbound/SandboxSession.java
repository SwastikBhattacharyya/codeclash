package com.codeclash.runnerservice.submission.port.outbound;

import com.codeclash.runnerservice.submission.model.Code;
import com.codeclash.runnerservice.submission.model.Compilation;
import com.codeclash.runnerservice.submission.model.TestRun;
import com.codeclash.contracts.problem.submission.Language;

public interface SandboxSession extends AutoCloseable {

  void writeSource(Language language, Code code);

  Compilation compile(Language language);

  void limitMemory(int megabytes);

  TestRun run(Language language, int timeLimitMs, String input);

  @Override
  void close();
}

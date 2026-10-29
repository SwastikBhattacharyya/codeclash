package com.codeclash.runnerservice.submission.adapter.outbound.sandbox;

import com.codeclash.runnerservice.submission.model.Code;
import com.codeclash.runnerservice.submission.model.Compilation;
import com.codeclash.runnerservice.submission.model.TestRun;
import com.codeclash.runnerservice.submission.model.TestVerdict;
import com.codeclash.runnerservice.submission.model.exception.SandboxFailureException;
import com.codeclash.runnerservice.submission.port.outbound.SandboxProvider;
import com.codeclash.runnerservice.submission.port.outbound.SandboxSession;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.runnerservice.sandbox.api.CompileOutcome;
import com.codeclash.runnerservice.sandbox.api.CompileResult;
import com.codeclash.runnerservice.sandbox.api.RunResult;
import com.codeclash.runnerservice.sandbox.api.Sandbox;
import com.codeclash.runnerservice.sandbox.api.SandboxPool;
import com.codeclash.runnerservice.sandbox.api.exception.SandboxException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SandboxApiAdapter implements SandboxProvider {

  private final SandboxPool sandboxPool;

  private static <T> T translate(Supplier<T> action) {
    try {
      return action.get();
    } catch (SandboxException exception) {
      throw new SandboxFailureException(exception.getMessage());
    }
  }

  private static TestVerdict toVerdict(RunResult result) {
    return switch (result.verdict()) {
      case OK -> TestVerdict.OK;
      case TLE -> TestVerdict.TIME_LIMIT_EXCEEDED;
      case MLE -> TestVerdict.MEMORY_LIMIT_EXCEEDED;
      case RE -> TestVerdict.RUNTIME_ERROR;
    };
  }

  @Override
  public SandboxSession open() {
    return new Session(SandboxApiAdapter.translate(this.sandboxPool::acquire));
  }

  private record Session(Sandbox sandbox) implements SandboxSession {

    @Override
    public void writeSource(Language language, Code code) {
      SandboxApiAdapter.translate(
          () -> {
            this.sandbox.writeSource(language, code.value());
            return null;
          });
    }

    @Override
    public Compilation compile(Language language) {
      CompileResult result = SandboxApiAdapter.translate(() -> this.sandbox.compile(language));
      return switch (result.outcome()) {
        case CompileOutcome.COMPILED -> Compilation.success();
        case CompileOutcome.COMPILATION_ERROR -> Compilation.failure(result.output());
      };
    }

    @Override
    public void limitMemory(int megabytes) {
      SandboxApiAdapter.translate(
          () -> {
            this.sandbox.limitMemory(megabytes);
            return null;
          });
    }

    @Override
    public TestRun run(Language language, int timeLimitMs, String input) {
      RunResult result =
          SandboxApiAdapter.translate(() -> this.sandbox.run(language, timeLimitMs, input));
      return new TestRun(
          SandboxApiAdapter.toVerdict(result), result.timeMs(), result.memoryKb(), result.output());
    }

    @Override
    public void close() {
      SandboxApiAdapter.translate(
          () -> {
            this.sandbox.close();
            return null;
          });
    }
  }
}

package com.codeclash.runnerservice.sandbox.adapter.outbound.docker;

import com.codeclash.runnerservice.sandbox.adapter.config.SandboxProperties;
import com.codeclash.runnerservice.sandbox.api.CompileResult;
import com.codeclash.runnerservice.sandbox.api.RunResult;
import com.codeclash.runnerservice.sandbox.api.RunVerdict;
import com.codeclash.runnerservice.sandbox.api.Sandbox;
import com.codeclash.runnerservice.sandbox.api.exception.SandboxException;
import com.codeclash.contracts.problem.submission.Language;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DockerSandbox implements Sandbox {

  private static final String SOURCE_DIR = "/sandbox/src/";
  private static final String INPUT_FILE = "/sandbox/in.txt";
  private static final String OUTPUT_FILE = "/sandbox/out.txt";
  private static final String COMPILE_SCRIPT = "/opt/runner/compile.sh";
  private static final String RUN_SCRIPT = "/opt/runner/run.sh";
  private static final String WRITE_SCRIPT =
      "umask 022; mkdir -p \"$(dirname \"$1\")\" && cat > \"$1\"";
  private static final int EXIT_COMPILED = 0;
  private static final int EXIT_COMPILATION_ERROR = 1;
  private static final String NUL = "\u0000";
  private static final int CRASH_OUTPUT_MAX_BYTES = 262_145;
  private static final int RUN_RESULT_FIELDS = 3;
  private static final String WRITE_SOURCE_OBSERVATION = "sandbox.source.write";
  private static final String COMPILE_OBSERVATION = "sandbox.compile";
  private static final String LIMIT_MEMORY_OBSERVATION = "sandbox.memory.limit";
  private static final String RUN_OBSERVATION = "sandbox.run";
  private static final String CLOSE_OBSERVATION = "sandbox.close";
  private static final String LANGUAGE_KEY = "language";
  private static final String RESULT_KEY = "result";
  private static final String VERDICT_KEY = "verdict";
  private static final String ERROR_VALUE = "error";

  private final DockerCli dockerCli;
  private final SandboxProperties properties;
  private final ObservationRegistry observationRegistry;
  private final String containerId;

  DockerSandbox(
      DockerCli dockerCli,
      SandboxProperties properties,
      ObservationRegistry observationRegistry,
      String containerId) {
    this.dockerCli = dockerCli;
    this.properties = properties;
    this.observationRegistry = observationRegistry;
    this.containerId = containerId;
  }

  private static String sourceFile(Language language) {
    return switch (language) {
      case CPP -> "Main.cpp";
      case JAVA -> "Main.java";
      case PYTHON -> "Main.py";
    };
  }

  private static String tagValue(Enum<?> value) {
    return value.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public void writeSource(Language language, String code) {
    String path = DockerSandbox.SOURCE_DIR + DockerSandbox.sourceFile(language);
    this.observation(DockerSandbox.WRITE_SOURCE_OBSERVATION, language)
        .observe(() -> this.writeFile(path, code));
  }

  @Override
  public CompileResult compile(Language language) {
    Observation observation =
        this.observation(DockerSandbox.COMPILE_OBSERVATION, language)
            .lowCardinalityKeyValue(DockerSandbox.RESULT_KEY, DockerSandbox.ERROR_VALUE);
    return observation.observe(
        () -> {
          CompileResult result = this.compileInContainer(language);
          observation.lowCardinalityKeyValue(
              DockerSandbox.RESULT_KEY, DockerSandbox.tagValue(result.outcome()));
          return result;
        });
  }

  @Override
  public void limitMemory(int megabytes) {
    Observation.createNotStarted(DockerSandbox.LIMIT_MEMORY_OBSERVATION, this.observationRegistry)
        .observe(() -> this.limitMemoryInContainer(megabytes));
  }

  @Override
  public RunResult run(Language language, int timeLimitMs, String input) {
    Observation observation =
        this.observation(DockerSandbox.RUN_OBSERVATION, language)
            .lowCardinalityKeyValue(DockerSandbox.VERDICT_KEY, DockerSandbox.ERROR_VALUE);
    return observation.observe(
        () -> {
          RunResult result = this.runInContainer(language, timeLimitMs, input);
          observation.lowCardinalityKeyValue(
              DockerSandbox.VERDICT_KEY, DockerSandbox.tagValue(result.verdict()));
          return result;
        });
  }

  @Override
  public void close() {
    Observation.createNotStarted(DockerSandbox.CLOSE_OBSERVATION, this.observationRegistry)
        .observe(this::removeContainer);
  }

  private Observation observation(String name, Language language) {
    return Observation.createNotStarted(name, this.observationRegistry)
        .lowCardinalityKeyValue(DockerSandbox.LANGUAGE_KEY, language.value());
  }

  private CompileResult compileInContainer(Language language) {
    Duration timeout = this.properties.compileTimeout().plus(this.properties.dockerTimeoutSlack());
    DockerResult result =
        this.dockerCli.execute(
            List.of(
                "exec",
                this.containerId,
                DockerSandbox.COMPILE_SCRIPT,
                language.value(),
                Long.toString(Math.max(1, this.properties.compileTimeout().toSeconds()))),
            null,
            timeout);
    if (result.exitCode() == DockerSandbox.EXIT_COMPILED) return CompileResult.compiled();
    if (result.exitCode() == DockerSandbox.EXIT_COMPILATION_ERROR && result.stderr().isBlank())
      return CompileResult.compilationError(result.stdout().replace(DockerSandbox.NUL, ""));
    throw new SandboxException(
        "compile.sh exited with %d: %s".formatted(result.exitCode(), result.stderr().strip()));
  }

  private void limitMemoryInContainer(int megabytes) {
    String limit = megabytes + "m";
    this.dockerCli.executeChecked(
        List.of("update", "--memory", limit, "--memory-swap", limit, this.containerId),
        null,
        this.properties.dockerTimeoutSlack());
  }

  private RunResult runInContainer(Language language, int timeLimitMs, String input) {
    this.writeFile(DockerSandbox.INPUT_FILE, input);
    Duration timeout = Duration.ofMillis(timeLimitMs).plus(this.properties.dockerTimeoutSlack());
    DockerResult result =
        this.dockerCli.executeChecked(
            List.of(
                "exec",
                this.containerId,
                DockerSandbox.RUN_SCRIPT,
                language.value(),
                Integer.toString(timeLimitMs),
                DockerSandbox.INPUT_FILE,
                DockerSandbox.OUTPUT_FILE),
            null,
            timeout);
    String[] fields = result.stdout().strip().split(" ");
    if (fields.length != DockerSandbox.RUN_RESULT_FIELDS)
      throw new SandboxException("unexpected run.sh output: " + result.stdout().strip());
    RunVerdict verdict;
    long timeMs;
    long memoryKb;
    try {
      verdict = RunVerdict.valueOf(fields[0]);
      timeMs = Long.parseLong(fields[1]);
      memoryKb = Long.parseLong(fields[2]);
    } catch (IllegalArgumentException exception) {
      throw new SandboxException("unexpected run.sh output: " + result.stdout().strip());
    }
    String output =
        switch (verdict) {
          case RunVerdict.OK -> this.readFile(DockerSandbox.OUTPUT_FILE);
          case RunVerdict.RE -> this.readCrashOutput();
          case RunVerdict.TLE, RunVerdict.MLE -> null;
        };
    this.dockerCli.executeChecked(
        List.of("exec", this.containerId, "rm", "-f", DockerSandbox.INPUT_FILE, DockerSandbox.OUTPUT_FILE),
        null,
        this.properties.dockerTimeoutSlack());
    return new RunResult(verdict, timeMs, memoryKb, output);
  }

  private void removeContainer() {
    try {
      this.dockerCli.execute(
          List.of("rm", "-f", this.containerId), null, this.properties.dockerTimeoutSlack());
    } catch (SandboxException exception) {
      DockerSandbox.log
          .atWarn()
          .setMessage("sandbox removal failed")
          .addKeyValue("container.id", this.containerId)
          .addKeyValue("error.message", exception.getMessage())
          .log();
    }
  }

  private void writeFile(String path, String content) {
    this.dockerCli.executeChecked(
        List.of("exec", "-i", this.containerId, "sh", "-c", DockerSandbox.WRITE_SCRIPT, "sh", path),
        content,
        this.properties.dockerTimeoutSlack());
  }

  private String readCrashOutput() {
    try {
      return this.dockerCli
          .executeChecked(
              List.of(
                  "exec",
                  this.containerId,
                  "head",
                  "-c",
                  Integer.toString(DockerSandbox.CRASH_OUTPUT_MAX_BYTES),
                  DockerSandbox.OUTPUT_FILE),
              null,
              this.properties.dockerTimeoutSlack())
          .stdout()
          .replace(DockerSandbox.NUL, "");
    } catch (SandboxException exception) {
      DockerSandbox.log
          .atWarn()
          .setMessage("crash output read failed")
          .addKeyValue("container.id", this.containerId)
          .addKeyValue("error.message", exception.getMessage())
          .log();
      return null;
    }
  }

  private String readFile(String path) {
    return this.dockerCli
        .executeChecked(
            List.of("exec", this.containerId, "cat", path), null, this.properties.dockerTimeoutSlack())
        .stdout();
  }
}

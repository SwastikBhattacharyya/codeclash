package com.codeclash.runnerservice.sandbox.adapter.outbound.docker;

import com.codeclash.runnerservice.sandbox.api.exception.SandboxException;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DockerCli {

  private static final String DOCKER = "docker";
  private static final String FAILURES_METRIC = "docker.command.failures";
  private static final String SUBCOMMAND_KEY = "subcommand";
  private static final String REASON_KEY = "reason";
  private static final String START_REASON = "start";
  private static final String TIMEOUT_REASON = "timeout";
  private static final String IO_REASON = "io";
  private static final String INTERRUPTED_REASON = "interrupted";
  private static final String EXIT_REASON = "exit";

  private final MeterRegistry meterRegistry;

  DockerResult execute(List<String> arguments, String stdin, Duration timeout) {
    List<String> command = new ArrayList<>();
    command.add(DockerCli.DOCKER);
    command.addAll(arguments);
    Process process;
    try {
      process = new ProcessBuilder(command).start();
    } catch (IOException exception) {
      this.failed(arguments, DockerCli.START_REASON);
      throw new SandboxException("failed to start docker: " + exception.getMessage());
    }
    try (ExecutorService streams = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<byte[]> stdout = streams.submit(() -> process.getInputStream().readAllBytes());
      Future<byte[]> stderr = streams.submit(() -> process.getErrorStream().readAllBytes());
      try (OutputStream input = process.getOutputStream()) {
        if (stdin != null) input.write(stdin.getBytes(StandardCharsets.UTF_8));
      }
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        this.failed(arguments, DockerCli.TIMEOUT_REASON);
        throw new SandboxException("docker %s timed out after %s".formatted(arguments.getFirst(), timeout));
      }
      return new DockerResult(
          process.exitValue(),
          new String(stdout.get(), StandardCharsets.UTF_8),
          new String(stderr.get(), StandardCharsets.UTF_8));
    } catch (IOException | ExecutionException exception) {
      process.destroyForcibly();
      this.failed(arguments, DockerCli.IO_REASON);
      throw new SandboxException("docker %s failed: %s".formatted(arguments.getFirst(), exception.getMessage()));
    } catch (InterruptedException exception) {
      process.destroyForcibly();
      this.failed(arguments, DockerCli.INTERRUPTED_REASON);
      Thread.currentThread().interrupt();
      throw new SandboxException("docker %s interrupted".formatted(arguments.getFirst()));
    }
  }

  DockerResult executeChecked(List<String> arguments, String stdin, Duration timeout) {
    DockerResult result = this.execute(arguments, stdin, timeout);
    if (result.exitCode() == 0) return result;
    this.failed(arguments, DockerCli.EXIT_REASON);
    throw new SandboxException(
        "docker %s exited with %d: %s"
            .formatted(arguments.getFirst(), result.exitCode(), result.stderr().strip()));
  }

  private void failed(List<String> arguments, String reason) {
    this.meterRegistry
        .counter(
            DockerCli.FAILURES_METRIC,
            DockerCli.SUBCOMMAND_KEY,
            arguments.getFirst(),
            DockerCli.REASON_KEY,
            reason)
        .increment();
  }
}

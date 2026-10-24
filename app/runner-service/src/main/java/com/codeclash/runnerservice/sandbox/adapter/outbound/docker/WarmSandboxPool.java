package com.codeclash.runnerservice.sandbox.adapter.outbound.docker;

import com.codeclash.runnerservice.sandbox.adapter.config.SandboxProperties;
import com.codeclash.runnerservice.sandbox.api.Sandbox;
import com.codeclash.runnerservice.sandbox.api.SandboxPool;
import com.codeclash.runnerservice.sandbox.api.exception.SandboxException;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class WarmSandboxPool implements SandboxPool, SmartLifecycle {

  private static final String LABEL_KEY = "codeclash.runner";
  private static final int PHASE = Integer.MAX_VALUE - 2000;
  private static final String ACQUIRE_OBSERVATION = "sandbox.acquire";
  private static final String SOURCE_KEY = "source";
  private static final String WARM_SOURCE = "warm";
  private static final String COLD_SOURCE = "cold";
  private static final String IDLE_METRIC = "sandbox.pool.idle";
  private static final String PENDING_METRIC = "sandbox.pool.pending";
  private static final String TARGET_METRIC = "sandbox.pool.target";
  private static final String REPLENISH_FAILURES_METRIC = "sandbox.replenish.failures";
  private static final String REAPED_METRIC = "sandbox.reaped";

  private final DockerCli dockerCli;
  private final SandboxProperties properties;
  private final ObservationRegistry observationRegistry;
  private final MeterRegistry meterRegistry;
  private final BlockingQueue<String> warm = new LinkedBlockingQueue<>();
  private final AtomicInteger pending = new AtomicInteger();
  private ExecutorService replenisher;
  private volatile boolean running;

  @Override
  public void start() {
    Gauge.builder(WarmSandboxPool.IDLE_METRIC, this.warm, BlockingQueue::size)
        .register(this.meterRegistry);
    Gauge.builder(WarmSandboxPool.PENDING_METRIC, this.pending, AtomicInteger::get)
        .register(this.meterRegistry);
    Gauge.builder(WarmSandboxPool.TARGET_METRIC, this.properties::warmSize)
        .register(this.meterRegistry);
    this.reap();
    this.replenisher = Executors.newVirtualThreadPerTaskExecutor();
    this.running = true;
    for (int i = 0; i < this.properties.warmSize(); i++) this.replenisher.submit(this::replenish);
    WarmSandboxPool.log
        .atInfo()
        .setMessage("sandbox pool started")
        .addKeyValue("warm.size", this.properties.warmSize())
        .log();
  }

  @Override
  public void stop() {
    this.running = false;
    if (this.replenisher != null) this.replenisher.shutdownNow();
    this.warm.clear();
    this.reap();
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }

  @Override
  public int getPhase() {
    return WarmSandboxPool.PHASE;
  }

  @Override
  public Sandbox acquire() {
    Observation observation =
        Observation.createNotStarted(WarmSandboxPool.ACQUIRE_OBSERVATION, this.observationRegistry);
    return observation.observe(() -> this.acquire(observation));
  }

  private Sandbox acquire(Observation observation) {
    String containerId = this.warm.poll();
    observation.lowCardinalityKeyValue(
        WarmSandboxPool.SOURCE_KEY,
        containerId == null ? WarmSandboxPool.COLD_SOURCE : WarmSandboxPool.WARM_SOURCE);
    if (containerId == null) containerId = this.create();
    try {
      if (this.running) this.replenisher.submit(this::replenish);
    } catch (RejectedExecutionException exception) {
      this.remove(List.of(containerId));
      throw new SandboxException("sandbox pool is stopping");
    }
    return new DockerSandbox(
        this.dockerCli, this.properties, this.observationRegistry, containerId);
  }

  private void replenish() {
    if (!this.running) return;
    if (this.warm.size() + this.pending.incrementAndGet() > this.properties.warmSize()) {
      this.pending.decrementAndGet();
      return;
    }
    String containerId;
    try {
      containerId = this.create();
    } catch (RuntimeException exception) {
      this.pending.decrementAndGet();
      this.meterRegistry.counter(WarmSandboxPool.REPLENISH_FAILURES_METRIC).increment();
      WarmSandboxPool.log
          .atWarn()
          .setMessage("warm sandbox start failed")
          .addKeyValue("error.message", exception.getMessage())
          .log();
      return;
    }
    this.pending.decrementAndGet();
    if (this.running) this.warm.offer(containerId);
    else this.remove(List.of(containerId));
  }

  private String create() {
    String tmpfsSize = this.properties.tmpfsSize().toMegabytes() + "m";
    String memory = this.properties.memory().toMegabytes() + "m";
    List<String> arguments =
        List.of(
            "run",
            "-d",
            "--init",
            "--label",
            WarmSandboxPool.LABEL_KEY + "=" + this.properties.instanceLabel(),
            "--network",
            "none",
            "--read-only",
            "--tmpfs",
            "/sandbox:rw,exec,size=" + tmpfsSize + ",mode=755",
            "--tmpfs",
            "/tmp:rw,exec,size=" + tmpfsSize + ",mode=1777",
            "--cap-drop",
            "ALL",
            "--cap-add",
            "CHOWN",
            "--cap-add",
            "FOWNER",
            "--cap-add",
            "SETUID",
            "--cap-add",
            "SETGID",
            "--security-opt",
            "no-new-privileges",
            "--pids-limit",
            Integer.toString(this.properties.pidsLimit()),
            "--cpus",
            Double.toString(this.properties.cpus()),
            "--memory",
            memory,
            "--memory-swap",
            memory,
            this.properties.image());
    return this.dockerCli
        .executeChecked(arguments, null, this.properties.dockerTimeoutSlack())
        .stdout()
        .strip();
  }

  private void reap() {
    String filter = "label=" + WarmSandboxPool.LABEL_KEY + "=" + this.properties.instanceLabel();
    try {
      String ids =
          this.dockerCli
              .executeChecked(
                  List.of("ps", "-aq", "--filter", filter), null, this.properties.dockerTimeoutSlack())
              .stdout();
      List<String> containerIds =
          Arrays.stream(ids.split("\\s+")).filter(id -> !id.isBlank()).toList();
      if (containerIds.isEmpty()) return;
      if (!this.remove(containerIds)) {
        WarmSandboxPool.log
            .atWarn()
            .setMessage("sandbox reap failed")
            .addKeyValue("count", containerIds.size())
            .log();
        return;
      }
      this.meterRegistry.counter(WarmSandboxPool.REAPED_METRIC).increment(containerIds.size());
      WarmSandboxPool.log
          .atInfo()
          .setMessage("sandboxes reaped")
          .addKeyValue("count", containerIds.size())
          .log();
    } catch (RuntimeException exception) {
      WarmSandboxPool.log
          .atWarn()
          .setMessage("sandbox reap failed")
          .addKeyValue("error.message", exception.getMessage())
          .log();
    }
  }

  private boolean remove(List<String> containerIds) {
    List<String> arguments = new ArrayList<>(List.of("rm", "-f"));
    arguments.addAll(containerIds);
    return this.dockerCli.execute(arguments, null, this.properties.dockerTimeoutSlack()).exitCode()
        == 0;
  }
}

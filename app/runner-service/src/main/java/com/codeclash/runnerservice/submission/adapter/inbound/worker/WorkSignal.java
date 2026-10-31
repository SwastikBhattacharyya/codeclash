package com.codeclash.runnerservice.submission.adapter.inbound.worker;

import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class WorkSignal {

  private final Object lock = new Object();
  private boolean pending;

  public void signal() {
    synchronized (this.lock) {
      this.pending = true;
      this.lock.notifyAll();
    }
  }

  void await(Duration timeout) throws InterruptedException {
    synchronized (this.lock) {
      if (!this.pending) this.lock.wait(timeout.toMillis());
      this.pending = false;
    }
  }
}

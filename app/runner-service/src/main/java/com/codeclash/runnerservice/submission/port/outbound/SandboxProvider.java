package com.codeclash.runnerservice.submission.port.outbound;

public interface SandboxProvider {

  SandboxSession open();
}

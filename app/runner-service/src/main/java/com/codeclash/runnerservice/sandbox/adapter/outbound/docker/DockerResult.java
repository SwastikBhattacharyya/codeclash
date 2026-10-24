package com.codeclash.runnerservice.sandbox.adapter.outbound.docker;

record DockerResult(int exitCode, String stdout, String stderr) {}

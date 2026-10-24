package com.codeclash.runnerservice.sandbox.api;

public record RunResult(RunVerdict verdict, long timeMs, long memoryKb, String output) {}

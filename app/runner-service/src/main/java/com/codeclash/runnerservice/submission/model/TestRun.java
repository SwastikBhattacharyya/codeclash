package com.codeclash.runnerservice.submission.model;

public record TestRun(TestVerdict verdict, long timeMs, long memoryKb, String output) {}

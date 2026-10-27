package com.codeclash.runnerservice.submission.model;

public record JudgeTestCase(
    TestCaseId id, String input, ExpectedOutput expectedOutput, boolean isPublic) {}

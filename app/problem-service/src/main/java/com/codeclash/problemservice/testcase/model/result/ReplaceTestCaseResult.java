package com.codeclash.problemservice.testcase.model.result;

import com.codeclash.problemservice.testcase.model.TestCase;

public record ReplaceTestCaseResult(TestCase testCase, boolean created) {}

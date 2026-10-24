package com.codeclash.runnerservice.testcase.api;

import java.util.UUID;

public record TestCaseView(UUID id, String input, String output, boolean isPublic) {}

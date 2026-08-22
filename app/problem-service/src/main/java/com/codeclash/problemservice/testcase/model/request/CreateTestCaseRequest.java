package com.codeclash.problemservice.testcase.model.request;

import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;

public record CreateTestCaseRequest(
    QuestionId questionId, Input input, Output output, Public isPublic) {}

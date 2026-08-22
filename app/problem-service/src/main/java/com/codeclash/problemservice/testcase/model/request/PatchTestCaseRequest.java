package com.codeclash.problemservice.testcase.model.request;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.Input;
import com.codeclash.problemservice.testcase.model.Output;
import com.codeclash.problemservice.testcase.model.Public;
import com.codeclash.problemservice.testcase.model.QuestionId;

public record PatchTestCaseRequest(
    Id id,
    QuestionId questionId,
    PatchObject<Input> input,
    PatchObject<Output> output,
    PatchObject<Public> isPublic) {}

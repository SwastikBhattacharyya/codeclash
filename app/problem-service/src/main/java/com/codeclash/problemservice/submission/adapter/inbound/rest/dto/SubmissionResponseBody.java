package com.codeclash.problemservice.submission.adapter.inbound.rest.dto;

import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.ActualOutput;
import com.codeclash.problemservice.submission.model.Code;
import com.codeclash.problemservice.submission.model.CompileOutput;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.Submission;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A submission and its judging result")
public record SubmissionResponseBody(
    @Schema(
            description = "Submission id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f50-4f5a-7b6c-8d7e-8f9a0b1c2d3e")
        UUID id,
    @Schema(
            description = "Programming language of the code",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            implementation = Language.class,
            example = "python")
        String language,
    @Schema(
            description = "Source code",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            maxLength = Code.MAX_SIZE,
            example = "print(sum(map(int, input().split())))")
        String code,
    @Schema(
            description = "Judging state",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            implementation = SubmissionState.class,
            example = "correct")
        String state,
    @Schema(
            description =
                "First test case that failed; may be set for incorrect, runtime_error, "
                    + "time_limit_exceeded and memory_limit_exceeded, null otherwise or once that"
                    + " test case is deleted",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            format = "uuid",
            example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
        UUID failedTestCaseId,
    @Schema(
            description =
                "Run time in milliseconds; only set when the state is correct, null "
                    + "otherwise",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"integer", "null"},
            format = "int64",
            example = "42")
        Long timeTakenMs,
    @Schema(
            description =
                "Peak memory in kilobytes; only set when the state is correct, null "
                    + "otherwise",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"integer", "null"},
            format = "int64",
            example = "10240")
        Long memoryUsedKb,
    @Schema(
            description =
                "Compiler console output, stdout and stderr combined; only set when the state is "
                    + "compilation_error, null otherwise. May be empty. Output over the limit is "
                    + "cut and ends with a truncation marker",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            maxLength = CompileOutput.MAX_SIZE,
            example = "/sandbox/src/Main.cpp: In function 'int main()':\n"
                + "/sandbox/src/Main.cpp:3:5: error: 'x' was not declared in this scope\n")
        String compileOutput,
    @Schema(
            description =
                "Standard output of the program on the failing test; only set for incorrect and "
                    + "runtime_error (partial output before the crash) when the failing test case "
                    + "is public, or when the caller can read private test cases, null otherwise. "
                    + "May be empty. Output over the limit is cut and ends with a truncation marker",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            maxLength = ActualOutput.MAX_SIZE,
            example = "3\n")
        String actualOutput,
    @Schema(
            description = "Id of the question",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID questionId,
    @Schema(
            description = "Username of the user who submitted",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "ada_lovelace")
        String username,
    @Schema(
            description = "When the submission was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the submission was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static SubmissionResponseBody from(Submission submission) {
    return new SubmissionResponseBody(
        submission.id().value(),
        submission.language().value(),
        submission.code().value(),
        submission.state().value(),
        submission.failedTestCaseId() == null ? null : submission.failedTestCaseId().value(),
        submission.timeTaken() == null ? null : submission.timeTaken().value(),
        submission.memoryUsed() == null ? null : submission.memoryUsed().value(),
        submission.compileOutput() == null ? null : submission.compileOutput().value(),
        submission.actualOutput() == null ? null : submission.actualOutput().value(),
        submission.questionId().value(),
        submission.username().value(),
        submission.createdAt().value(),
        submission.updatedAt().value());
  }
}

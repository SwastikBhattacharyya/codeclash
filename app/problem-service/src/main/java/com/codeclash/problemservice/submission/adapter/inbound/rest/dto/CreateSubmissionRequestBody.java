package com.codeclash.problemservice.submission.adapter.inbound.rest.dto;

import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.problemservice.submission.model.Code;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.exception.InvalidLanguageException;
import com.codeclash.problemservice.submission.model.exception.NullLanguageException;
import com.codeclash.problemservice.submission.model.owner.Unowned;
import com.codeclash.problemservice.submission.model.request.CreateSubmissionRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    description =
        "Code to judge against a question; the submission belongs to the caller and starts as"
            + " queued")
public record CreateSubmissionRequestBody(
    @Schema(
            description = "Id of the question to solve (UUID version " + QuestionId.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID questionId,
    @Schema(
            description = "Programming language of the code",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Language.class,
            example = "python")
        String language,
    @Schema(
            description = "Source code; must not be blank",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            maxLength = Code.MAX_SIZE,
            example = "print(sum(map(int, input().split())))")
        String code) {

  public CreateSubmissionRequest<Unowned> toRequest() {
    if (this.language == null) throw new NullLanguageException();
    return new CreateSubmissionRequest<>(
        QuestionId.of(this.questionId),
        Language.find(this.language).orElseThrow(() -> new InvalidLanguageException(this.language)),
        Code.of(this.code),
        new Unowned());
  }
}

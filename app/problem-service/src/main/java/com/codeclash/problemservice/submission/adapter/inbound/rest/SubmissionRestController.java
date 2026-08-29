package com.codeclash.problemservice.submission.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.page.Page;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.page.Size;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.sort.Direction;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.contracts.auth.session.BearerSessionFilter;
import com.codeclash.contracts.auth.session.rest.AuthApiExamples;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.adapter.inbound.rest.dto.CreateSubmissionRequestBody;
import com.codeclash.problemservice.submission.adapter.inbound.rest.dto.SubmissionResponseBody;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.UserId;
import com.codeclash.problemservice.submission.model.exception.InvalidLanguageException;
import com.codeclash.problemservice.submission.model.exception.InvalidSubmissionStateException;
import com.codeclash.problemservice.submission.model.request.QuerySubmissionRequest;
import com.codeclash.problemservice.submission.port.inbound.CreateSubmissionSecuredUseCase;
import com.codeclash.problemservice.submission.port.inbound.DeleteSubmissionSecuredUseCase;
import com.codeclash.problemservice.submission.port.inbound.GetSubmissionSecuredUseCase;
import com.codeclash.problemservice.submission.port.inbound.QuerySubmissionSecuredUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Submissions",
    description = "Code submitted against questions and its judging results")
@RestController
@RequestMapping(path = "/api/v{version}/submissions", version = "1+")
@RequiredArgsConstructor
public class SubmissionRestController {

  private final QuerySubmissionSecuredUseCase querySubmissionSecuredUseCase;
  private final GetSubmissionSecuredUseCase getSubmissionSecuredUseCase;
  private final CreateSubmissionSecuredUseCase createSubmissionSecuredUseCase;
  private final DeleteSubmissionSecuredUseCase deleteSubmissionSecuredUseCase;

  @Operation(
      summary = "List submissions",
      description =
          "Pages through submissions from every user matching every given filter. A session"
              + " is optional; without the "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " permission, submissions on private questions are left out.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching submissions"),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid filter, paging or sorting. Codes: INVALID_SUBMISSION_ID, "
                + "NULL_SUBMISSION_ID, INVALID_QUESTION_ID, NULL_QUESTION_ID, INVALID_USER_ID, "
                + "NULL_USER_ID, INVALID_LANGUAGE, INVALID_SUBMISSION_STATE, PAGE_TOO_SMALL, "
                + "SIZE_TOO_SMALL, SIZE_TOO_LARGE, UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_SUBMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_SUBMISSION_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.NULL_SUBMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.NULL_SUBMISSION_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.NULL_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.NULL_QUESTION_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_USER_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_USER_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.NULL_USER_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.NULL_USER_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_LANGUAGE,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_LANGUAGE),
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_SUBMISSION_STATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + SubmissionApiExamples.INVALID_SUBMISSION_STATE),
                  @ExampleObject(
                      name = ApiExamples.PAGE_TOO_SMALL,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.PAGE_TOO_SMALL),
                  @ExampleObject(
                      name = ApiExamples.SIZE_TOO_SMALL,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.SIZE_TOO_SMALL),
                  @ExampleObject(
                      name = ApiExamples.SIZE_TOO_LARGE,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.SIZE_TOO_LARGE),
                  @ExampleObject(
                      name = SubmissionApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.UNKNOWN_SORT_FIELD),
                  @ExampleObject(
                      name = ApiExamples.INVALID_SORT_DIRECTION,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.INVALID_SORT_DIRECTION)
                })),
    @ApiResponse(
        responseCode = "503",
        description = "Authentication service is unavailable. Codes: AUTH_UNAVAILABLE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = AuthApiExamples.AUTH_UNAVAILABLE,
                      ref = ApiExamples.REF_PREFIX + AuthApiExamples.AUTH_UNAVAILABLE)
                }))
  })
  @GetMapping
  ResponseEntity<PagedEntity<SubmissionResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only submissions with one of these ids; repeat for several",
              example = "01936f50-4f5a-7b6c-8d7e-8f9a0b1c2d3e")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Question IDs",
              description = "Only submissions to one of these questions; repeat for several",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @RequestParam(name = "question_id", required = false, defaultValue = "")
          List<UUID> questionIds,
      @Parameter(
              name = "User IDs",
              description = "Only submissions by one of these users; repeat for several",
              example = "01936f51-5a6b-7c7d-9e8f-9a0b1c2d3e4f")
          @RequestParam(name = "user_id", required = false, defaultValue = "")
          List<UUID> userIds,
      @Parameter(
              name = "Usernames",
              description =
                  "Only submissions by one of these usernames, matched exactly and"
                      + " case-sensitively; repeat for several",
              example = "ada_lovelace")
          @RequestParam(name = "username", required = false, defaultValue = "")
          List<String> usernames,
      @Parameter(
              name = "Languages",
              description = "Only submissions in one of these languages; repeat for several",
              array = @ArraySchema(schema = @Schema(implementation = Language.class)))
          @RequestParam(name = "language", required = false, defaultValue = "")
          List<String> languages,
      @Parameter(
              name = "States",
              description = "Only submissions in one of these judging states; repeat for several",
              array = @ArraySchema(schema = @Schema(implementation = SubmissionState.class)))
          @RequestParam(name = "state", required = false, defaultValue = "")
          List<String> states,
      @Parameter(
              name = "Created From",
              description = "Only submissions created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only submissions created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only submissions last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only submissions last changed at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_1", required = false)
          Instant maxUpdatedAt,
      @Parameter(
              name = "Page",
              description = "Page number, starting at " + Page.MIN,
              schema =
                  @Schema(
                      type = "integer",
                      minimum = "" + Page.MIN,
                      defaultValue = Page.DEFAULT))
          @RequestParam(name = "page", defaultValue = Page.DEFAULT)
          int page,
      @Parameter(
              name = "Page Size",
              description = "Maximum number of submissions per page",
              schema =
                  @Schema(
                      type = "integer",
                      minimum = "" + Size.MIN,
                      maximum = "" + Size.MAX,
                      defaultValue = Size.DEFAULT))
          @RequestParam(name = "size", defaultValue = Size.DEFAULT)
          int size,
      @Parameter(
              name = "Sort By",
              description = "Field to sort by",
              schema =
                  @Schema(
                      allowableValues = {"id", "language", "state", "created_at", "updated_at"},
                      defaultValue = "id"))
          @RequestParam(name = "sort", defaultValue = "id")
          String sort,
      @Parameter(
              name = "Sort Direction",
              description = "Sort direction",
              schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
          @RequestParam(name = "direction", defaultValue = "asc")
          String direction) {
    QuerySubmissionRequest request =
        QuerySubmissionRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .questionIds(questionIds.stream().map(QuestionId::of).toList())
            .userIds(userIds.stream().map(UserId::of).toList())
            .usernames(usernames)
            .languages(SubmissionRestController.languages(languages))
            .states(SubmissionRestController.states(states))
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<SubmissionResponseBody> submissions =
        this.querySubmissionSecuredUseCase.query(request).map(SubmissionResponseBody::from);
    return ResponseEntity.ok(submissions);
  }

  @Operation(
      summary = "Get a submission",
      description =
          "Returns one submission by id. A session is optional; submissions on private "
              + "questions need the "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The submission"),
    @ApiResponse(
        responseCode = "403",
        description =
            "The question is private and the session lacks the permission. Codes: "
                + "FORBIDDEN; properties.permissions names the missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + SubmissionApiExamples.FORBIDDEN_QUESTION_PRIVATE)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Submission does not exist. Codes: SUBMISSION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.SUBMISSION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.SUBMISSION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid submission id. Codes: INVALID_SUBMISSION_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_SUBMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_SUBMISSION_ID)
                })),
    @ApiResponse(
        responseCode = "503",
        description = "Authentication service is unavailable. Codes: AUTH_UNAVAILABLE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = AuthApiExamples.AUTH_UNAVAILABLE,
                      ref = ApiExamples.REF_PREFIX + AuthApiExamples.AUTH_UNAVAILABLE)
                }))
  })
  @GetMapping("{ID}")
  ResponseEntity<SubmissionResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Submission id (UUID version " + Id.VERSION + ")",
              example = "01936f50-4f5a-7b6c-8d7e-8f9a0b1c2d3e")
          @PathVariable("ID")
          UUID id) {
    Submission submission = this.getSubmissionSecuredUseCase.findById(Id.of(id));
    return ResponseEntity.ok(SubmissionResponseBody.from(submission));
  }

  @Operation(
      summary = "Submit code",
      description =
          "Creates a queued submission owned by the caller. Requires the "
              + ProblemPermissions.SUBMISSION_WRITE
              + " permission; submitting to a private question also needs "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + ".")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Submission queued",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created submission",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/submissions/01936f50-4f5a-7b6c-8d7e-8f9a0b1c2d3e"))),
    @ApiResponse(
        responseCode = "401",
        description = "No valid session. Codes: UNAUTHENTICATED",
        headers =
            @Header(
                name = HttpHeaders.WWW_AUTHENTICATE,
                description = "Authentication scheme to use",
                schema = @Schema(type = "string", example = BearerSessionFilter.SCHEME)),
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = AuthApiExamples.UNAUTHENTICATED,
                      ref = ApiExamples.REF_PREFIX + AuthApiExamples.UNAUTHENTICATED)
                })),
    @ApiResponse(
        responseCode = "403",
        description =
            "Session lacks a required permission. Codes: FORBIDDEN; properties.permissions "
                + "names the missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = SubmissionApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + SubmissionApiExamples.FORBIDDEN_QUESTION_PRIVATE)
                })),
    @ApiResponse(
        responseCode = "503",
        description = "Authentication service is unavailable. Codes: AUTH_UNAVAILABLE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = AuthApiExamples.AUTH_UNAVAILABLE,
                      ref = ApiExamples.REF_PREFIX + AuthApiExamples.AUTH_UNAVAILABLE)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid field or unknown question or user. Codes: NULL_QUESTION_ID, "
                + "INVALID_QUESTION_ID, QUESTION_NOT_FOUND, NULL_LANGUAGE, INVALID_LANGUAGE, "
                + "NULL_CODE, BLANK_CODE, CODE_TOO_LONG, USER_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.NULL_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.NULL_QUESTION_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = SubmissionApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.QUESTION_NOT_FOUND),
                  @ExampleObject(
                      name = SubmissionApiExamples.NULL_LANGUAGE,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.NULL_LANGUAGE),
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_LANGUAGE,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_LANGUAGE),
                  @ExampleObject(
                      name = SubmissionApiExamples.NULL_CODE,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.NULL_CODE),
                  @ExampleObject(
                      name = SubmissionApiExamples.BLANK_CODE,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.BLANK_CODE),
                  @ExampleObject(
                      name = SubmissionApiExamples.CODE_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.CODE_TOO_LONG),
                  @ExampleObject(
                      name = SubmissionApiExamples.USER_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.USER_NOT_FOUND)
                }))
  })
  @PostMapping
  ResponseEntity<SubmissionResponseBody> create(@RequestBody CreateSubmissionRequestBody body) {
    Submission submission = this.createSubmissionSecuredUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(submission.id().value())
            .toUri();
    return ResponseEntity.created(location).body(SubmissionResponseBody.from(submission));
  }

  @Operation(
      summary = "Delete a submission",
      description =
          "Requires a session. Deleting your own submission needs "
              + ProblemPermissions.SUBMISSION_DELETE_OWN
              + "; deleting someone else's needs "
              + ProblemPermissions.SUBMISSION_DELETE
              + ".")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Submission deleted"),
    @ApiResponse(
        responseCode = "401",
        description = "No valid session. Codes: UNAUTHENTICATED",
        headers =
            @Header(
                name = HttpHeaders.WWW_AUTHENTICATE,
                description = "Authentication scheme to use",
                schema = @Schema(type = "string", example = BearerSessionFilter.SCHEME)),
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = AuthApiExamples.UNAUTHENTICATED,
                      ref = ApiExamples.REF_PREFIX + AuthApiExamples.UNAUTHENTICATED)
                })),
    @ApiResponse(
        responseCode = "403",
        description =
            "Session lacks the delete permission for this submission. Codes: FORBIDDEN; "
                + "properties.permissions names the missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.FORBIDDEN_DELETE,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.FORBIDDEN_DELETE),
                  @ExampleObject(
                      name = SubmissionApiExamples.FORBIDDEN_DELETE_OWN,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.FORBIDDEN_DELETE_OWN)
                })),
    @ApiResponse(
        responseCode = "503",
        description = "Authentication service is unavailable. Codes: AUTH_UNAVAILABLE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = AuthApiExamples.AUTH_UNAVAILABLE,
                      ref = ApiExamples.REF_PREFIX + AuthApiExamples.AUTH_UNAVAILABLE)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Submission does not exist. Codes: SUBMISSION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.SUBMISSION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.SUBMISSION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid submission id. Codes: INVALID_SUBMISSION_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SubmissionApiExamples.INVALID_SUBMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + SubmissionApiExamples.INVALID_SUBMISSION_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "Submission id (UUID version " + Id.VERSION + ")",
              example = "01936f50-4f5a-7b6c-8d7e-8f9a0b1c2d3e")
          @PathVariable("ID")
          UUID id) {
    this.deleteSubmissionSecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }

  private static List<Language> languages(List<String> values) {
    return values.stream()
        .map(value -> Language.find(value).orElseThrow(() -> new InvalidLanguageException(value)))
        .toList();
  }

  private static List<SubmissionState> states(List<String> values) {
    return values.stream()
        .map(value ->
            SubmissionState.find(value)
                .orElseThrow(() -> new InvalidSubmissionStateException(value)))
        .toList();
  }
}

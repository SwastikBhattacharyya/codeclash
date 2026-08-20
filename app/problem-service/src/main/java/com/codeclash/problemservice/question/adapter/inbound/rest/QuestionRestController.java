package com.codeclash.problemservice.question.adapter.inbound.rest;

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
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.CreateQuestionRequestBody;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.PatchQuestionRequestBody;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.QuestionResponseBody;
import com.codeclash.problemservice.question.adapter.inbound.rest.dto.ReplaceQuestionRequestBody;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.category.CategoryId;
import com.codeclash.problemservice.question.model.request.QueryQuestionRequest;
import com.codeclash.problemservice.question.model.result.ReplaceQuestionResult;
import com.codeclash.problemservice.question.model.tag.TagId;
import com.codeclash.problemservice.question.port.inbound.CreateQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.DeleteQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.GetQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.PatchQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.QueryQuestionSecuredUseCase;
import com.codeclash.problemservice.question.port.inbound.ReplaceQuestionSecuredUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Questions",
    description = "Coding problems with their limits, categories and tags")
@RestController
@RequestMapping(path = "/api/v{version}/questions", version = "1+")
@RequiredArgsConstructor
public class QuestionRestController {

  private final QueryQuestionSecuredUseCase queryQuestionSecuredUseCase;
  private final GetQuestionSecuredUseCase getQuestionSecuredUseCase;
  private final CreateQuestionSecuredUseCase createQuestionSecuredUseCase;
  private final ReplaceQuestionSecuredUseCase replaceQuestionSecuredUseCase;
  private final PatchQuestionSecuredUseCase patchQuestionSecuredUseCase;
  private final DeleteQuestionSecuredUseCase deleteQuestionSecuredUseCase;

  @Operation(
      summary = "List questions",
      description =
          "Pages through questions matching every given filter. A session is optional; "
              + "without the "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " permission only visible questions are returned.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching questions"),
    @ApiResponse(
        responseCode = "403",
        description =
            "visible=false was requested without the "
                + ProblemPermissions.QUESTION_READ_IS_PRIVATE
                + " permission. Codes: FORBIDDEN; properties.permissions names the missing "
                + "permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN_READ_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN_READ_IS_PRIVATE)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_QUESTION_ID, "
                + "NULL_QUESTION_ID, NULL_CATEGORIES, INVALID_CATEGORY_ID, NULL_TAGS, "
                + "INVALID_TAG_ID, PAGE_TOO_SMALL, SIZE_TOO_SMALL, SIZE_TOO_LARGE, "
                + "UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_QUESTION_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_CATEGORIES,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_CATEGORIES),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_CATEGORY_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_TAGS,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_TAGS),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_TAG_ID),
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
                      name = QuestionApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.UNKNOWN_SORT_FIELD),
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
  ResponseEntity<PagedEntity<QuestionResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only questions with one of these ids; repeat for several",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Title",
              description =
                  "Only questions whose title contains one of these terms, ignoring case; "
                      + "repeat for several",
              example = "sum")
          @RequestParam(name = "title", required = false, defaultValue = "")
          List<String> titles,
      @Parameter(
              name = "Summary",
              description =
                  "Only questions whose summary contains one of these terms, ignoring case;"
                      + " repeat for several",
              example = "array")
          @RequestParam(name = "summary", required = false, defaultValue = "")
          List<String> summaries,
      @Parameter(
              name = "Description",
              description =
                  "Only questions whose description contains one of these terms, ignoring "
                      + "case; repeat for several",
              example = "indices")
          @RequestParam(name = "description", required = false, defaultValue = "")
          List<String> descriptions,
      @Parameter(
              name = "Visible",
              description =
                  "Only visible (true) or private (false) questions. Without the "
                      + ProblemPermissions.QUESTION_READ_IS_PRIVATE
                      + " permission, results are limited to visible questions and false is "
                      + "rejected",
              example = "true")
          @RequestParam(name = "visible", required = false, defaultValue = "")
          Boolean visible,
      @Parameter(
              name = "Category IDs",
              description =
                  "Only questions in at least one of these categories; repeat for several",
              example = "01936f41-2d3e-7f4a-9b5c-6d7e8f9a0b1c")
          @RequestParam(name = "category", required = false, defaultValue = "")
          List<UUID> categoryIds,
      @Parameter(
              name = "Tag IDs",
              description = "Only questions with at least one of these tags; repeat for several",
              example = "01936f42-3e4f-7a5b-8c6d-7e8f9a0b1c2d")
          @RequestParam(name = "tag", required = false, defaultValue = "")
          List<UUID> tagIds,
      @Parameter(
              name = "Created From",
              description = "Only questions created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only questions created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only questions last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only questions last changed at or before this instant",
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
              description = "Maximum number of questions per page",
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
                      allowableValues = {
                        "id", "title", "summary", "visible", "created_at", "updated_at"
                      },
                      defaultValue = "id"))
          @RequestParam(name = "sort", defaultValue = "id")
          String sort,
      @Parameter(
              name = "Sort Direction",
              description = "Sort direction",
              schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
          @RequestParam(name = "direction", defaultValue = "asc")
          String direction) {
    QueryQuestionRequest request =
        QueryQuestionRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .titles(titles)
            .summaries(summaries)
            .descriptions(descriptions)
            .visible(visible)
            .categoryIds(categoryIds.stream().map(CategoryId::of).toList())
            .tagIds(tagIds.stream().map(TagId::of).toList())
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<QuestionResponseBody> questions =
        this.queryQuestionSecuredUseCase.query(request).map(QuestionResponseBody::from);
    return ResponseEntity.ok(questions);
  }

  @Operation(
      summary = "Get a question",
      description =
          "Returns one question by id. A session is optional; private questions need the "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The question"),
    @ApiResponse(
        responseCode = "403",
        description =
            "The question is private and the session lacks the "
                + ProblemPermissions.QUESTION_READ_IS_PRIVATE
                + " permission. Codes: FORBIDDEN; properties.permissions names the missing "
                + "permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN_READ_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN_READ_IS_PRIVATE)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Question does not exist. Codes: QUESTION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.QUESTION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid question id. Codes: INVALID_QUESTION_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_QUESTION_ID)
                })),
    @ApiResponse(
        responseCode = "503",
        description =
            "Authentication service is unavailable while checking access to a private "
                + "question. Codes: AUTH_UNAVAILABLE",
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
  ResponseEntity<QuestionResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Question id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id) {
    Question question =
        this.getQuestionSecuredUseCase.findById(Id.of(id));
    return ResponseEntity.ok(QuestionResponseBody.from(question));
  }

  @Operation(
      summary = "Create a question",
      description =
          "Requires the "
              + ProblemPermissions.QUESTION_WRITE
              + " permission. Also needs "
              + ProblemPermissions.QUESTION_WRITE_IS_PRIVATE
              + " when the result is private (isVisible false, or omitted, which defaults to "
              + "false).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Question created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created question",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/questions/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f"))),
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
                + "names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN),
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE)
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
            "Invalid field or unknown category or tag (categories are checked first). Codes: "
                + "NULL_TITLE, TITLE_TOO_SHORT, "
                + "TITLE_TOO_LONG, INVALID_TITLE, NULL_SUMMARY, SUMMARY_TOO_SHORT, "
                + "SUMMARY_TOO_LONG, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, "
                + "DESCRIPTION_TOO_LONG, MAX_RUN_TIME_TOO_LOW, MAX_RUN_TIME_TOO_HIGH, "
                + "MAX_RAM_LIMIT_TOO_LOW, MAX_RAM_LIMIT_TOO_HIGH, NULL_CATEGORIES, "
                + "INVALID_CATEGORY_ID, CATEGORIES_NOT_FOUND, NULL_TAGS, INVALID_TAG_ID, "
                + "TAGS_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_TITLE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_TITLE),
                  @ExampleObject(
                      name = QuestionApiExamples.TITLE_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TITLE_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.TITLE_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TITLE_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_TITLE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_TITLE),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_SUMMARY,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_SUMMARY),
                  @ExampleObject(
                      name = QuestionApiExamples.SUMMARY_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.SUMMARY_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.SUMMARY_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.SUMMARY_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = QuestionApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RUN_TIME_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RUN_TIME_TOO_LOW),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RUN_TIME_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RUN_TIME_TOO_HIGH),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RAM_LIMIT_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RAM_LIMIT_TOO_LOW),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RAM_LIMIT_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RAM_LIMIT_TOO_HIGH),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_CATEGORIES,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_CATEGORIES),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_CATEGORY_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.CATEGORIES_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.CATEGORIES_NOT_FOUND),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_TAGS,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_TAGS),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_TAG_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.TAGS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TAGS_NOT_FOUND)
                }))
  })
  @PostMapping
  ResponseEntity<QuestionResponseBody> create(@RequestBody CreateQuestionRequestBody body) {
    Question question =
        this.createQuestionSecuredUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(question.id().value())
            .toUri();
    return ResponseEntity.created(location).body(QuestionResponseBody.from(question));
  }

  @Operation(
      summary = "Replace a question",
      description =
          "Replaces every field of the question, or creates it under the given id when it "
              + "does not exist. Requires the "
              + ProblemPermissions.QUESTION_WRITE
              + " permission. Also needs "
              + ProblemPermissions.QUESTION_WRITE_IS_PRIVATE
              + " when the result is private (isVisible false, or omitted, which defaults to "
              + "false), or when the stored question is already private. Setting isVisible to "
              + "false while the question is in a running contest is rejected.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Question replaced"),
    @ApiResponse(
        responseCode = "201",
        description = "Question created under the given id",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created question",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/questions/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f"))),
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
                + "names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN),
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE)
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
        responseCode = "409",
        description =
            "Setting isVisible to false (or omitting it on replace) while the question is "
                + "in a running contest. Codes: QUESTION_IN_RUNNING_CONTEST",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.QUESTION_IN_RUNNING_CONTEST,
                      ref =
                          ApiExamples.REF_PREFIX
                              + QuestionApiExamples.QUESTION_IN_RUNNING_CONTEST)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field or unknown category or tag (categories are checked first)."
                + " Codes: INVALID_QUESTION_ID, "
                + "NULL_TITLE, TITLE_TOO_SHORT, TITLE_TOO_LONG, INVALID_TITLE, NULL_SUMMARY, "
                + "SUMMARY_TOO_SHORT, SUMMARY_TOO_LONG, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, "
                + "DESCRIPTION_TOO_LONG, MAX_RUN_TIME_TOO_LOW, MAX_RUN_TIME_TOO_HIGH, "
                + "MAX_RAM_LIMIT_TOO_LOW, MAX_RAM_LIMIT_TOO_HIGH, NULL_CATEGORIES, "
                + "INVALID_CATEGORY_ID, CATEGORIES_NOT_FOUND, NULL_TAGS, INVALID_TAG_ID, "
                + "TAGS_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_TITLE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_TITLE),
                  @ExampleObject(
                      name = QuestionApiExamples.TITLE_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TITLE_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.TITLE_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TITLE_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_TITLE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_TITLE),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_SUMMARY,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_SUMMARY),
                  @ExampleObject(
                      name = QuestionApiExamples.SUMMARY_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.SUMMARY_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.SUMMARY_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.SUMMARY_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = QuestionApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RUN_TIME_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RUN_TIME_TOO_LOW),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RUN_TIME_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RUN_TIME_TOO_HIGH),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RAM_LIMIT_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RAM_LIMIT_TOO_LOW),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RAM_LIMIT_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RAM_LIMIT_TOO_HIGH),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_CATEGORIES,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_CATEGORIES),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_CATEGORY_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.CATEGORIES_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.CATEGORIES_NOT_FOUND),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_TAGS,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_TAGS),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_TAG_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.TAGS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TAGS_NOT_FOUND)
                }))
  })
  @PutMapping("{ID}")
  ResponseEntity<QuestionResponseBody> replace(
      @Parameter(
              name = "ID",
              description = "Question id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id,
      @RequestBody ReplaceQuestionRequestBody body) {
    ReplaceQuestionResult result =
        this.replaceQuestionSecuredUseCase.replace(body.toRequest(id));
    QuestionResponseBody response = QuestionResponseBody.from(result.question());
    if (!result.created()) return ResponseEntity.ok(response);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build(true).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Update part of a question",
      description =
          "Changes only the fields present in the body. Requires the "
              + ProblemPermissions.QUESTION_WRITE
              + " permission. Also needs "
              + ProblemPermissions.QUESTION_WRITE_IS_PRIVATE
              + " when isVisible is set to false or the stored question is already private. "
              + "Setting isVisible to false while the question is in a running contest is "
              + "rejected.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Question updated"),
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
                + "names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN),
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE)
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
        description = "Question does not exist. Codes: QUESTION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.QUESTION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description =
            "Setting isVisible to false (or omitting it on replace) while the question is "
                + "in a running contest. Codes: QUESTION_IN_RUNNING_CONTEST",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.QUESTION_IN_RUNNING_CONTEST,
                      ref =
                          ApiExamples.REF_PREFIX
                              + QuestionApiExamples.QUESTION_IN_RUNNING_CONTEST)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field or unknown category or tag (categories are checked first)."
                + " Codes: INVALID_QUESTION_ID, "
                + "NULL_TITLE, TITLE_TOO_SHORT, TITLE_TOO_LONG, INVALID_TITLE, NULL_SUMMARY, "
                + "SUMMARY_TOO_SHORT, SUMMARY_TOO_LONG, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, "
                + "DESCRIPTION_TOO_LONG, NULL_VISIBLE, NULL_MAX_RUN_TIME, NULL_MAX_RAM_LIMIT, "
                + "MAX_RUN_TIME_TOO_LOW, MAX_RUN_TIME_TOO_HIGH, MAX_RAM_LIMIT_TOO_LOW, "
                + "MAX_RAM_LIMIT_TOO_HIGH, NULL_CATEGORIES, INVALID_CATEGORY_ID, "
                + "CATEGORIES_NOT_FOUND, NULL_TAGS, INVALID_TAG_ID, TAGS_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_TITLE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_TITLE),
                  @ExampleObject(
                      name = QuestionApiExamples.TITLE_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TITLE_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.TITLE_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TITLE_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_TITLE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_TITLE),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_SUMMARY,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_SUMMARY),
                  @ExampleObject(
                      name = QuestionApiExamples.SUMMARY_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.SUMMARY_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.SUMMARY_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.SUMMARY_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = QuestionApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = QuestionApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_VISIBLE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_VISIBLE),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_MAX_RUN_TIME,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_MAX_RUN_TIME),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_MAX_RAM_LIMIT,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_MAX_RAM_LIMIT),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RUN_TIME_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RUN_TIME_TOO_LOW),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RUN_TIME_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RUN_TIME_TOO_HIGH),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RAM_LIMIT_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RAM_LIMIT_TOO_LOW),
                  @ExampleObject(
                      name = QuestionApiExamples.MAX_RAM_LIMIT_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.MAX_RAM_LIMIT_TOO_HIGH),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_CATEGORIES,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_CATEGORIES),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_CATEGORY_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.CATEGORIES_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.CATEGORIES_NOT_FOUND),
                  @ExampleObject(
                      name = QuestionApiExamples.NULL_TAGS,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.NULL_TAGS),
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_TAG_ID),
                  @ExampleObject(
                      name = QuestionApiExamples.TAGS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.TAGS_NOT_FOUND)
                }))
  })
  @PatchMapping("{ID}")
  ResponseEntity<QuestionResponseBody> patch(
      @Parameter(
              name = "ID",
              description = "Question id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id,
      @RequestBody PatchQuestionRequestBody body) {
    Question question =
        this.patchQuestionSecuredUseCase.patch(body.toRequest(id));
    return ResponseEntity.ok(QuestionResponseBody.from(question));
  }

  @Operation(
      summary = "Delete a question",
      description =
          "Requires the "
              + ProblemPermissions.QUESTION_WRITE
              + " permission; deleting a private question also needs "
              + ProblemPermissions.QUESTION_WRITE_IS_PRIVATE
              + ". A question in a running contest cannot be deleted.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Question deleted"),
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
                + "names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN),
                  @ExampleObject(
                      name = QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.FORBIDDEN_WRITE_IS_PRIVATE)
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
        description = "Question does not exist. Codes: QUESTION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.QUESTION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description = "The question is in a running contest. Codes: QUESTION_IN_RUNNING_CONTEST",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.QUESTION_IN_RUNNING_CONTEST,
                      ref =
                          ApiExamples.REF_PREFIX
                              + QuestionApiExamples.QUESTION_IN_RUNNING_CONTEST)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid question id. Codes: INVALID_QUESTION_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = QuestionApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + QuestionApiExamples.INVALID_QUESTION_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "Question id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id) {
    this.deleteQuestionSecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

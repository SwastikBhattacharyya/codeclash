package com.codeclash.problemservice.testcase.adapter.inbound.rest;

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
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.CreateTestCaseRequestBody;
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.PatchTestCaseRequestBody;
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.ReplaceTestCaseRequestBody;
import com.codeclash.problemservice.testcase.adapter.inbound.rest.dto.TestCaseResponseBody;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import com.codeclash.problemservice.testcase.model.TestCase;
import com.codeclash.problemservice.testcase.model.request.QueryTestCaseRequest;
import com.codeclash.problemservice.testcase.model.result.ReplaceTestCaseResult;
import com.codeclash.problemservice.testcase.port.inbound.CreateTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.DeleteTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.GetTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.PatchTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.QueryTestCaseSecuredUseCase;
import com.codeclash.problemservice.testcase.port.inbound.ReplaceTestCaseSecuredUseCase;
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
    name = "Test cases",
    description = "Inputs and expected outputs used to judge a question's submissions")
@RestController
@RequestMapping(path = "/api/v{version}/questions/{QuestionID}/test-cases", version = "1+")
@RequiredArgsConstructor
public class TestCaseRestController {

  private final QueryTestCaseSecuredUseCase queryTestCaseSecuredUseCase;
  private final GetTestCaseSecuredUseCase getTestCaseSecuredUseCase;
  private final CreateTestCaseSecuredUseCase createTestCaseSecuredUseCase;
  private final ReplaceTestCaseSecuredUseCase replaceTestCaseSecuredUseCase;
  private final PatchTestCaseSecuredUseCase patchTestCaseSecuredUseCase;
  private final DeleteTestCaseSecuredUseCase deleteTestCaseSecuredUseCase;

  @Operation(
      summary = "List a question's test cases",
      description =
          "Pages through the question's test cases matching every given filter. A session "
              + "is optional; a private question needs the "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " permission, and without "
              + ProblemPermissions.TEST_CASE_READ_IS_PRIVATE
              + " only public test cases are listed.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching test cases"),
    @ApiResponse(
        responseCode = "403",
        description =
            "The question is private, or is_public=false was requested, without the needed "
                + "permission. Codes: FORBIDDEN; properties.permissions names the missing "
                + "permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_READ_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.FORBIDDEN_READ_IS_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_AND_READ_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_AND_READ_IS_PRIVATE)
                })),
    @ApiResponse(
        responseCode = "404",
        description =
            "Question does not exist; only checked when the session cannot read private "
                + "questions, otherwise the page is empty. Codes: QUESTION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.QUESTION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, filter, paging or sorting. Codes: INVALID_QUESTION_ID, "
                + "INVALID_TEST_CASE_ID, NULL_TEST_CASE_ID, PAGE_TOO_SMALL, SIZE_TOO_SMALL, "
                + "SIZE_TOO_LARGE, UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_TEST_CASE_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_TEST_CASE_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_TEST_CASE_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_TEST_CASE_ID),
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
                      name = TestCaseApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.UNKNOWN_SORT_FIELD),
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
  ResponseEntity<PagedEntity<TestCaseResponseBody>> findAll(
      @Parameter(
              name = "QuestionID",
              description = "Question id (UUID version " + QuestionId.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("QuestionID")
          UUID questionId,
      @Parameter(
              name = "IDs",
              description = "Only test cases with one of these ids; repeat for several",
              example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Public",
              description =
                  "Only public (true) or private (false) test cases. Without the "
                      + ProblemPermissions.TEST_CASE_READ_IS_PRIVATE
                      + " permission, results are limited to public test cases and false is "
                      + "rejected",
              example = "true")
          @RequestParam(name = "is_public", required = false, defaultValue = "")
          Boolean isPublic,
      @Parameter(
              name = "Created From",
              description = "Only test cases created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only test cases created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only test cases last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only test cases last changed at or before this instant",
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
              description = "Maximum number of test cases per page",
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
                      allowableValues = {"id", "is_public", "created_at", "updated_at"},
                      defaultValue = "id"))
          @RequestParam(name = "sort", defaultValue = "id")
          String sort,
      @Parameter(
              name = "Sort Direction",
              description = "Sort direction",
              schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
          @RequestParam(name = "direction", defaultValue = "asc")
          String direction) {
    QueryTestCaseRequest request =
        QueryTestCaseRequest.builder()
            .questionId(QuestionId.of(questionId))
            .ids(ids.stream().map(Id::of).toList())
            .isPublic(isPublic)
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<TestCaseResponseBody> testCases =
        this.queryTestCaseSecuredUseCase.query(request).map(TestCaseResponseBody::from);
    return ResponseEntity.ok(testCases);
  }

  @Operation(
      summary = "Get a test case",
      description =
          "Returns one test case of the question. A session is optional; a private question"
              + " needs the "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " permission and a private test case needs "
              + ProblemPermissions.TEST_CASE_READ_IS_PRIVATE
              + ".")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The test case"),
    @ApiResponse(
        responseCode = "403",
        description =
            "The question or test case is private and the session lacks the permission. "
                + "Codes: FORBIDDEN; properties.permissions names the missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_READ_IS_PRIVATE,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.FORBIDDEN_READ_IS_PRIVATE)
                })),
    @ApiResponse(
        responseCode = "404",
        description =
            "Question does not exist, or the test case does not exist or belongs to another"
                + " question. Codes: QUESTION_NOT_FOUND, TEST_CASE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.QUESTION_NOT_FOUND),
                  @ExampleObject(
                      name = TestCaseApiExamples.TEST_CASE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.TEST_CASE_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid question or test case id. Codes: INVALID_QUESTION_ID, "
                + "INVALID_TEST_CASE_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_TEST_CASE_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_TEST_CASE_ID)
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
  ResponseEntity<TestCaseResponseBody> findById(
      @Parameter(
              name = "QuestionID",
              description = "Question id (UUID version " + QuestionId.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("QuestionID")
          UUID questionId,
      @Parameter(
              name = "ID",
              description = "Test case id (UUID version " + Id.VERSION + ")",
              example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
          @PathVariable("ID")
          UUID id) {
    TestCase testCase =
        this.getTestCaseSecuredUseCase.findById(QuestionId.of(questionId), Id.of(id));
    return ResponseEntity.ok(TestCaseResponseBody.from(testCase));
  }

  @Operation(
      summary = "Add a test case",
      description =
          "Requires the "
              + ProblemPermissions.TEST_CASE_WRITE
              + " permission, plus "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " when the question is private. A private test case (isPublic false or omitted, "
              + "which defaults to false) also needs "
              + ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE
              + ".")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Test case created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created test case",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example =
                            "/api/v1/questions/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f/test-cases/"
                                + "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a"))),
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
                      name = TestCaseApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE)
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
                      name = TestCaseApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.QUESTION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid question id or field. Codes: INVALID_QUESTION_ID, NULL_INPUT, "
                + "INPUT_TOO_LONG, NULL_OUTPUT, OUTPUT_TOO_LONG",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_INPUT,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_INPUT),
                  @ExampleObject(
                      name = TestCaseApiExamples.INPUT_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INPUT_TOO_LONG),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_OUTPUT,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_OUTPUT),
                  @ExampleObject(
                      name = TestCaseApiExamples.OUTPUT_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.OUTPUT_TOO_LONG)
                }))
  })
  @PostMapping
  ResponseEntity<TestCaseResponseBody> create(
      @Parameter(
              name = "QuestionID",
              description = "Question id (UUID version " + QuestionId.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("QuestionID")
          UUID questionId,
      @RequestBody CreateTestCaseRequestBody body) {
    TestCase testCase = this.createTestCaseSecuredUseCase.create(body.toRequest(questionId));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(testCase.id().value())
            .toUri();
    return ResponseEntity.created(location).body(TestCaseResponseBody.from(testCase));
  }

  @Operation(
      summary = "Replace a test case",
      description =
          "Replaces every field of the test case, or creates it under the given id when it "
              + "does not exist. Requires the "
              + ProblemPermissions.TEST_CASE_WRITE
              + " permission, plus "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " when the question is private. Also needs "
              + ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE
              + " when the result is private (isPublic false or omitted) or the stored test case "
              + "is private.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Test case replaced"),
    @ApiResponse(
        responseCode = "201",
        description = "Test case created under the given id",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created test case",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example =
                            "/api/v1/questions/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f/test-cases/"
                                + "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a"))),
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
                      name = TestCaseApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE)
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
        description =
            "Question does not exist, or the test case does not exist or belongs to another"
                + " question. Codes: QUESTION_NOT_FOUND, TEST_CASE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.QUESTION_NOT_FOUND),
                  @ExampleObject(
                      name = TestCaseApiExamples.TEST_CASE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.TEST_CASE_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id or field. Codes: INVALID_QUESTION_ID, INVALID_TEST_CASE_ID, "
                + "NULL_INPUT, INPUT_TOO_LONG, NULL_OUTPUT, OUTPUT_TOO_LONG",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_TEST_CASE_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_TEST_CASE_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_INPUT,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_INPUT),
                  @ExampleObject(
                      name = TestCaseApiExamples.INPUT_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INPUT_TOO_LONG),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_OUTPUT,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_OUTPUT),
                  @ExampleObject(
                      name = TestCaseApiExamples.OUTPUT_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.OUTPUT_TOO_LONG)
                }))
  })
  @PutMapping("{ID}")
  ResponseEntity<TestCaseResponseBody> replace(
      @Parameter(
              name = "QuestionID",
              description = "Question id (UUID version " + QuestionId.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("QuestionID")
          UUID questionId,
      @Parameter(
              name = "ID",
              description = "Test case id (UUID version " + Id.VERSION + ")",
              example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
          @PathVariable("ID")
          UUID id,
      @RequestBody ReplaceTestCaseRequestBody body) {
    ReplaceTestCaseResult result =
        this.replaceTestCaseSecuredUseCase.replace(body.toRequest(questionId, id));
    TestCaseResponseBody response = TestCaseResponseBody.from(result.testCase());
    if (!result.created()) return ResponseEntity.ok(response);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build(true).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Update part of a test case",
      description =
          "Changes only the fields present in the body. Requires the "
              + ProblemPermissions.TEST_CASE_WRITE
              + " permission, plus "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " when the question is private. Also needs "
              + ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE
              + " when isPublic is set to false or the stored test case is private.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Test case updated"),
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
                      name = TestCaseApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_AND_WRITE_IS_PRIVATE)
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
        description =
            "Question does not exist, or the test case does not exist or belongs to another"
                + " question. Codes: QUESTION_NOT_FOUND, TEST_CASE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.QUESTION_NOT_FOUND),
                  @ExampleObject(
                      name = TestCaseApiExamples.TEST_CASE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.TEST_CASE_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id or field. Codes: INVALID_QUESTION_ID, INVALID_TEST_CASE_ID, "
                + "NULL_INPUT, INPUT_TOO_LONG, NULL_OUTPUT, OUTPUT_TOO_LONG, NULL_PUBLIC",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_TEST_CASE_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_TEST_CASE_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_INPUT,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_INPUT),
                  @ExampleObject(
                      name = TestCaseApiExamples.INPUT_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INPUT_TOO_LONG),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_OUTPUT,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_OUTPUT),
                  @ExampleObject(
                      name = TestCaseApiExamples.OUTPUT_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.OUTPUT_TOO_LONG),
                  @ExampleObject(
                      name = TestCaseApiExamples.NULL_PUBLIC,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.NULL_PUBLIC)
                }))
  })
  @PatchMapping("{ID}")
  ResponseEntity<TestCaseResponseBody> patch(
      @Parameter(
              name = "QuestionID",
              description = "Question id (UUID version " + QuestionId.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("QuestionID")
          UUID questionId,
      @Parameter(
              name = "ID",
              description = "Test case id (UUID version " + Id.VERSION + ")",
              example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
          @PathVariable("ID")
          UUID id,
      @RequestBody PatchTestCaseRequestBody body) {
    TestCase testCase = this.patchTestCaseSecuredUseCase.patch(body.toRequest(questionId, id));
    return ResponseEntity.ok(TestCaseResponseBody.from(testCase));
  }

  @Operation(
      summary = "Delete a test case",
      description =
          "Requires the "
              + ProblemPermissions.TEST_CASE_WRITE
              + " permission, plus "
              + ProblemPermissions.QUESTION_READ_IS_PRIVATE
              + " when the question is private. Deleting a private test case also needs "
              + ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE
              + ".")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Test case deleted"),
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
                      name = TestCaseApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_QUESTION_PRIVATE),
                  @ExampleObject(
                      name = TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE,
                      ref =
                          ApiExamples.REF_PREFIX
                              + TestCaseApiExamples.FORBIDDEN_WRITE_IS_PRIVATE)
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
        description =
            "Question does not exist, or the test case does not exist or belongs to another"
                + " question. Codes: QUESTION_NOT_FOUND, TEST_CASE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.QUESTION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.QUESTION_NOT_FOUND),
                  @ExampleObject(
                      name = TestCaseApiExamples.TEST_CASE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.TEST_CASE_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid question or test case id. Codes: INVALID_QUESTION_ID, "
                + "INVALID_TEST_CASE_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = TestCaseApiExamples.INVALID_TEST_CASE_ID,
                      ref = ApiExamples.REF_PREFIX + TestCaseApiExamples.INVALID_TEST_CASE_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "QuestionID",
              description = "Question id (UUID version " + QuestionId.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("QuestionID")
          UUID questionId,
      @Parameter(
              name = "ID",
              description = "Test case id (UUID version " + Id.VERSION + ")",
              example = "01936f52-6b7c-7d8e-8f9a-0b1c2d3e4f5a")
          @PathVariable("ID")
          UUID id) {
    this.deleteTestCaseSecuredUseCase.delete(QuestionId.of(questionId), Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

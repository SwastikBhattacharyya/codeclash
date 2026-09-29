package com.codeclash.contestservice.contest.adapter.inbound.rest;

import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.page.Page;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.page.Size;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.sort.Direction;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestResponseBody;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ContestSummaryResponseBody;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.CreateContestRequestBody;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.LeaderboardEntryResponseBody;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.PatchContestRequestBody;
import com.codeclash.contestservice.contest.adapter.inbound.rest.dto.ReplaceContestRequestBody;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.request.QueryContestRequest;
import com.codeclash.contestservice.contest.model.result.ReplaceContestResult;
import com.codeclash.contestservice.contest.port.inbound.CreateContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.DeleteContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.GetContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.GetLeaderboardUseCase;
import com.codeclash.contestservice.contest.port.inbound.PatchContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.QueryContestUseCase;
import com.codeclash.contestservice.contest.port.inbound.RegisterContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.ReplaceContestSecuredUseCase;
import com.codeclash.contestservice.contest.port.inbound.UnregisterContestSecuredUseCase;
import com.codeclash.contracts.auth.session.BearerSessionFilter;
import com.codeclash.contracts.auth.session.rest.AuthApiExamples;
import com.codeclash.contracts.contest.ContestPermissions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
    name = "Contests",
    description = "Timed contests, registration and leaderboards")
@RestController
@RequestMapping(path = "/api/v{version}/contests", version = "1+")
@RequiredArgsConstructor
public class ContestRestController {

  private final QueryContestUseCase queryContestUseCase;
  private final GetContestSecuredUseCase getContestSecuredUseCase;
  private final CreateContestSecuredUseCase createContestSecuredUseCase;
  private final ReplaceContestSecuredUseCase replaceContestSecuredUseCase;
  private final PatchContestSecuredUseCase patchContestSecuredUseCase;
  private final DeleteContestSecuredUseCase deleteContestSecuredUseCase;
  private final GetLeaderboardUseCase getLeaderboardUseCase;
  private final RegisterContestSecuredUseCase registerContestSecuredUseCase;
  private final UnregisterContestSecuredUseCase unregisterContestSecuredUseCase;

  @Operation(
      summary = "List contests",
      description =
          "Pages through contests matching every given filter, without their questions. No "
              + "session is needed.")
  @SecurityRequirements
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching contests"),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_CONTEST_ID, "
                + "NULL_CONTEST_ID, PAGE_TOO_SMALL, SIZE_TOO_SMALL, SIZE_TOO_LARGE, "
                + "UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_CONTEST_ID),
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
                      name = ContestApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.UNKNOWN_SORT_FIELD),
                  @ExampleObject(
                      name = ApiExamples.INVALID_SORT_DIRECTION,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.INVALID_SORT_DIRECTION)
                }))
  })
  @GetMapping
  ResponseEntity<PagedEntity<ContestSummaryResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only contests with one of these ids; repeat for several",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Name",
              description =
                  "Only contests whose name contains one of these terms, ignoring case; "
                      + "repeat for several",
              example = "weekly")
          @RequestParam(name = "name", required = false, defaultValue = "")
          List<String> names,
      @Parameter(
              name = "Description",
              description =
                  "Only contests whose description contains one of these terms, ignoring "
                      + "case; repeat for several",
              example = "graphs")
          @RequestParam(name = "description", required = false, defaultValue = "")
          List<String> descriptions,
      @Parameter(
              name = "Live From",
              description = "Only contests going live at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "live_at_0", required = false)
          Instant minLiveAt,
      @Parameter(
              name = "Live Until",
              description = "Only contests going live at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "live_at_1", required = false)
          Instant maxLiveAt,
      @Parameter(
              name = "End From",
              description = "Only contests ending at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "end_at_0", required = false)
          Instant minEndAt,
      @Parameter(
              name = "End Until",
              description = "Only contests ending at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "end_at_1", required = false)
          Instant maxEndAt,
      @Parameter(
              name = "Created From",
              description = "Only contests created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only contests created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only contests last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only contests last changed at or before this instant",
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
              description = "Maximum number of contests per page",
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
                        "id", "name", "live_at", "end_at", "created_at", "updated_at"
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
    QueryContestRequest request =
        QueryContestRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .names(names)
            .descriptions(descriptions)
            .minLiveAt(minLiveAt)
            .maxLiveAt(maxLiveAt)
            .minEndAt(minEndAt)
            .maxEndAt(maxEndAt)
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<ContestSummaryResponseBody> contests =
        this.queryContestUseCase.query(request).map(ContestSummaryResponseBody::from);
    return ResponseEntity.ok(contests);
  }

  @Operation(
      summary = "Get a contest",
      description =
          "Returns one contest. A session is optional. Questions are null until the contest"
              + " has started unless the caller has the "
              + ContestPermissions.CONTEST_READ_QUESTIONS
              + " permission, and registered is only filled for a signed-in caller. A contest "
              + "counts as started once it is opened, about 30 seconds before liveAt.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The contest"),
    @ApiResponse(
        responseCode = "404",
        description = "Contest does not exist. Codes: CONTEST_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid contest id. Codes: INVALID_CONTEST_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID)
                })),
    @ApiResponse(
        responseCode = "503",
        description =
            "Authentication service is unavailable while checking access to the questions "
                + "of a contest that has not started. Codes: AUTH_UNAVAILABLE",
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
  ResponseEntity<ContestResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Contest id (UUID version " + Id.VERSION + ")",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @PathVariable("ID")
          UUID id) {
    return ResponseEntity.ok(
        ContestResponseBody.from(this.getContestSecuredUseCase.findById(Id.of(id))));
  }

  @Operation(
      summary = "Get the leaderboard",
      description =
          "Pages through the standings of every registered user. No session is needed. "
              + "Before liveAt the page is empty. Users are ranked by points, then by the "
              + "earliest last solve; ties share a rank and leave a gap. Only the first correct "
              + "submission per question counts, made after both liveAt and the user's "
              + "registration and before endAt.")
  @SecurityRequirements
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of standings"),
    @ApiResponse(
        responseCode = "404",
        description = "Contest does not exist. Codes: CONTEST_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid contest id or paging. Codes: INVALID_CONTEST_ID, PAGE_TOO_SMALL, "
                + "SIZE_TOO_SMALL, SIZE_TOO_LARGE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID),
                  @ExampleObject(
                      name = ApiExamples.PAGE_TOO_SMALL,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.PAGE_TOO_SMALL),
                  @ExampleObject(
                      name = ApiExamples.SIZE_TOO_SMALL,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.SIZE_TOO_SMALL),
                  @ExampleObject(
                      name = ApiExamples.SIZE_TOO_LARGE,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.SIZE_TOO_LARGE)
                }))
  })
  @GetMapping("{ID}/leaderboard")
  ResponseEntity<PagedEntity<LeaderboardEntryResponseBody>> findLeaderboard(
      @Parameter(
              name = "ID",
              description = "Contest id (UUID version " + Id.VERSION + ")",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @PathVariable("ID")
          UUID id,
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
              description = "Maximum number of standings per page",
              schema =
                  @Schema(
                      type = "integer",
                      minimum = "" + Size.MIN,
                      maximum = "" + Size.MAX,
                      defaultValue = Size.DEFAULT))
          @RequestParam(name = "size", defaultValue = Size.DEFAULT)
          int size) {
    PagedEntity<LeaderboardEntryResponseBody> entries =
        this.getLeaderboardUseCase
            .findPage(Id.of(id), new PaginationRequest(new Page(page), new Size(size)))
            .map(LeaderboardEntryResponseBody::from);
    return ResponseEntity.ok(entries);
  }

  @Operation(
      summary = "Register for a contest",
      description =
          "Registers the caller for the contest. Requires a session. Registration stays "
              + "open until endAt; registering late only counts solves made after registering.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Registered"),
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
            "Contest does not exist, or the user has not reached this service yet. Codes: "
                + "CONTEST_NOT_FOUND, REGISTRATION_USER_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_NOT_FOUND),
                  @ExampleObject(
                      name = ContestApiExamples.REGISTRATION_USER_NOT_FOUND,
                      ref =
                          ApiExamples.REF_PREFIX
                              + ContestApiExamples.REGISTRATION_USER_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description =
            "The contest has ended or the caller is already registered. Codes: "
                + "CONTEST_ENDED, ALREADY_REGISTERED",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_ENDED,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_ENDED),
                  @ExampleObject(
                      name = ContestApiExamples.ALREADY_REGISTERED,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.ALREADY_REGISTERED)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid contest id. Codes: INVALID_CONTEST_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID)
                }))
  })
  @PostMapping("{ID}/registration")
  ResponseEntity<Void> register(
      @Parameter(
              name = "ID",
              description = "Contest id (UUID version " + Id.VERSION + ")",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @PathVariable("ID")
          UUID id) {
    this.registerContestSecuredUseCase.register(Id.of(id));
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Unregister from a contest",
      description =
          "Removes the caller's registration. Requires a session. Not allowed once the "
              + "contest has started. A contest counts as started once it is opened, about 30 "
              + "seconds before liveAt.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Unregistered"),
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
            "Contest does not exist, or the caller is not registered. Codes: "
                + "CONTEST_NOT_FOUND, REGISTRATION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_NOT_FOUND),
                  @ExampleObject(
                      name = ContestApiExamples.REGISTRATION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.REGISTRATION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description = "The contest has started. Codes: CONTEST_ALREADY_LIVE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_ALREADY_LIVE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_ALREADY_LIVE)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid contest id. Codes: INVALID_CONTEST_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID)
                }))
  })
  @DeleteMapping("{ID}/registration")
  ResponseEntity<Void> unregister(
      @Parameter(
              name = "ID",
              description = "Contest id (UUID version " + Id.VERSION + ")",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @PathVariable("ID")
          UUID id) {
    this.unregisterContestSecuredUseCase.unregister(Id.of(id));
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Create a contest",
      description =
          "Requires the "
              + ContestPermissions.CONTEST_WRITE
              + " permission. liveAt and endAt must be in the future and endAt after liveAt; "
              + "questions must exist in this service. The response hides questions unless the "
              + "caller has "
              + ContestPermissions.CONTEST_READ_QUESTIONS
              + " or the contest has started.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Contest created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created contest",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/contests/01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f"))),
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
            "Session lacks the required permission. Codes: FORBIDDEN; "
                + "properties.permissions names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.FORBIDDEN_WRITE)
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
            "Another contest already has this name, ignoring case. Codes: "
                + "DUPLICATE_CONTEST_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.DUPLICATE_CONTEST_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DUPLICATE_CONTEST_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid field, schedule or questions. Codes: NULL_NAME, NAME_TOO_SHORT, "
                + "NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, "
                + "DESCRIPTION_TOO_LONG, NULL_LIVE_AT, NULL_END_AT, END_AT_NOT_AFTER_LIVE_AT, "
                + "LIVE_AT_IN_PAST, END_AT_IN_PAST, NULL_QUESTIONS, NULL_QUESTION_ID, "
                + "INVALID_QUESTION_ID, DUPLICATE_QUESTIONS, QUESTIONS_NOT_FOUND, NULL_POINTS, "
                + "POINTS_TOO_LOW, POINTS_TOO_HIGH",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = ContestApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = ContestApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = ContestApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_LIVE_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_LIVE_AT),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_END_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_END_AT),
                  @ExampleObject(
                      name = ContestApiExamples.END_AT_NOT_AFTER_LIVE_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.END_AT_NOT_AFTER_LIVE_AT),
                  @ExampleObject(
                      name = ContestApiExamples.LIVE_AT_IN_PAST,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.LIVE_AT_IN_PAST),
                  @ExampleObject(
                      name = ContestApiExamples.END_AT_IN_PAST,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.END_AT_IN_PAST),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_QUESTIONS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_QUESTIONS),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_QUESTION_ID),
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = ContestApiExamples.DUPLICATE_QUESTIONS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DUPLICATE_QUESTIONS),
                  @ExampleObject(
                      name = ContestApiExamples.QUESTIONS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.QUESTIONS_NOT_FOUND),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_POINTS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_POINTS),
                  @ExampleObject(
                      name = ContestApiExamples.POINTS_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.POINTS_TOO_LOW),
                  @ExampleObject(
                      name = ContestApiExamples.POINTS_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.POINTS_TOO_HIGH)
                }))
  })
  @PostMapping
  ResponseEntity<ContestResponseBody> create(@RequestBody CreateContestRequestBody body) {
    Contest contest = this.createContestSecuredUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(contest.id().value())
            .toUri();
    return ResponseEntity.created(location).body(ContestResponseBody.from(contest));
  }

  @Operation(
      summary = "Replace a contest",
      description =
          "Replaces every field of the contest, or creates it under the given id when it "
              + "does not exist. Requires the "
              + ContestPermissions.CONTEST_WRITE
              + " permission. Once the contest has started, liveAt and the questions (including "
              + "points) are locked; name, description and endAt stay editable. A changed liveAt "
              + "or endAt must be in the future. A contest counts as started once it is opened, "
              + "about 30 seconds before liveAt. The response hides questions unless the caller "
              + "has "
              + ContestPermissions.CONTEST_READ_QUESTIONS
              + " or the contest has started.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Contest replaced"),
    @ApiResponse(
        responseCode = "201",
        description = "Contest created under the given id",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created contest",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/contests/01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f"))),
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
            "Session lacks the required permission. Codes: FORBIDDEN; "
                + "properties.permissions names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.FORBIDDEN_WRITE)
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
            "Another contest already has this name, ignoring case, or the contest has "
                + "started and liveAt or questions changed. Codes: DUPLICATE_CONTEST_NAME, "
                + "CONTEST_ALREADY_LIVE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.DUPLICATE_CONTEST_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DUPLICATE_CONTEST_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_ALREADY_LIVE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_ALREADY_LIVE)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field, schedule or questions. Codes: INVALID_CONTEST_ID, "
                + "NULL_NAME, NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, "
                + "DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG, NULL_LIVE_AT, NULL_END_AT, "
                + "END_AT_NOT_AFTER_LIVE_AT, LIVE_AT_IN_PAST, END_AT_IN_PAST, NULL_QUESTIONS, "
                + "NULL_QUESTION_ID, INVALID_QUESTION_ID, DUPLICATE_QUESTIONS, "
                + "QUESTIONS_NOT_FOUND, NULL_POINTS, POINTS_TOO_LOW, POINTS_TOO_HIGH",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = ContestApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = ContestApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = ContestApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_LIVE_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_LIVE_AT),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_END_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_END_AT),
                  @ExampleObject(
                      name = ContestApiExamples.END_AT_NOT_AFTER_LIVE_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.END_AT_NOT_AFTER_LIVE_AT),
                  @ExampleObject(
                      name = ContestApiExamples.LIVE_AT_IN_PAST,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.LIVE_AT_IN_PAST),
                  @ExampleObject(
                      name = ContestApiExamples.END_AT_IN_PAST,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.END_AT_IN_PAST),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_QUESTIONS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_QUESTIONS),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_QUESTION_ID),
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = ContestApiExamples.DUPLICATE_QUESTIONS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DUPLICATE_QUESTIONS),
                  @ExampleObject(
                      name = ContestApiExamples.QUESTIONS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.QUESTIONS_NOT_FOUND),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_POINTS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_POINTS),
                  @ExampleObject(
                      name = ContestApiExamples.POINTS_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.POINTS_TOO_LOW),
                  @ExampleObject(
                      name = ContestApiExamples.POINTS_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.POINTS_TOO_HIGH)
                }))
  })
  @PutMapping("{ID}")
  ResponseEntity<ContestResponseBody> replace(
      @Parameter(
              name = "ID",
              description = "Contest id (UUID version " + Id.VERSION + ")",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @PathVariable("ID")
          UUID id,
      @RequestBody ReplaceContestRequestBody body) {
    ReplaceContestResult result = this.replaceContestSecuredUseCase.replace(body.toRequest(id));
    ContestResponseBody response = ContestResponseBody.from(result.contest());
    if (!result.created()) return ResponseEntity.ok(response);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build(true).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Update part of a contest",
      description =
          "Changes only the fields present in the body. Requires the "
              + ContestPermissions.CONTEST_WRITE
              + " permission. Once the contest has started, liveAt and the questions (including "
              + "points) are locked; name, description and endAt stay editable. A changed liveAt "
              + "or endAt must be in the future. A contest counts as started once it is opened, "
              + "about 30 seconds before liveAt. The response hides questions unless the caller "
              + "has "
              + ContestPermissions.CONTEST_READ_QUESTIONS
              + " or the contest has started.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Contest updated"),
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
            "Session lacks the required permission. Codes: FORBIDDEN; "
                + "properties.permissions names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.FORBIDDEN_WRITE)
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
        description = "Contest does not exist. Codes: CONTEST_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description =
            "Another contest already has this name, ignoring case, or the contest has "
                + "started and liveAt or questions changed. Codes: DUPLICATE_CONTEST_NAME, "
                + "CONTEST_ALREADY_LIVE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.DUPLICATE_CONTEST_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DUPLICATE_CONTEST_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_ALREADY_LIVE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_ALREADY_LIVE)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field, schedule or questions. Codes: INVALID_CONTEST_ID, "
                + "NULL_NAME, NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, "
                + "DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG, NULL_LIVE_AT, NULL_END_AT, "
                + "END_AT_NOT_AFTER_LIVE_AT, LIVE_AT_IN_PAST, END_AT_IN_PAST, NULL_QUESTIONS, "
                + "NULL_QUESTION_ID, INVALID_QUESTION_ID, DUPLICATE_QUESTIONS, "
                + "QUESTIONS_NOT_FOUND, NULL_POINTS, POINTS_TOO_LOW, POINTS_TOO_HIGH",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = ContestApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = ContestApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = ContestApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_LIVE_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_LIVE_AT),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_END_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_END_AT),
                  @ExampleObject(
                      name = ContestApiExamples.END_AT_NOT_AFTER_LIVE_AT,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.END_AT_NOT_AFTER_LIVE_AT),
                  @ExampleObject(
                      name = ContestApiExamples.LIVE_AT_IN_PAST,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.LIVE_AT_IN_PAST),
                  @ExampleObject(
                      name = ContestApiExamples.END_AT_IN_PAST,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.END_AT_IN_PAST),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_QUESTIONS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_QUESTIONS),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_QUESTION_ID),
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_QUESTION_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_QUESTION_ID),
                  @ExampleObject(
                      name = ContestApiExamples.DUPLICATE_QUESTIONS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.DUPLICATE_QUESTIONS),
                  @ExampleObject(
                      name = ContestApiExamples.QUESTIONS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.QUESTIONS_NOT_FOUND),
                  @ExampleObject(
                      name = ContestApiExamples.NULL_POINTS,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.NULL_POINTS),
                  @ExampleObject(
                      name = ContestApiExamples.POINTS_TOO_LOW,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.POINTS_TOO_LOW),
                  @ExampleObject(
                      name = ContestApiExamples.POINTS_TOO_HIGH,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.POINTS_TOO_HIGH)
                }))
  })
  @PatchMapping("{ID}")
  ResponseEntity<ContestResponseBody> patch(
      @Parameter(
              name = "ID",
              description = "Contest id (UUID version " + Id.VERSION + ")",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @PathVariable("ID")
          UUID id,
      @RequestBody PatchContestRequestBody body) {
    Contest contest = this.patchContestSecuredUseCase.patch(body.toRequest(id));
    return ResponseEntity.ok(ContestResponseBody.from(contest));
  }

  @Operation(
      summary = "Delete a contest",
      description =
          "Deletes the contest with its registrations and leaderboard. Requires the "
              + ContestPermissions.CONTEST_WRITE
              + " permission. Not allowed once the contest has started. A contest counts as "
              + "started once it is opened, about 30 seconds before liveAt.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Contest deleted"),
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
            "Session lacks the required permission. Codes: FORBIDDEN; "
                + "properties.permissions names the missing permission",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.FORBIDDEN_WRITE)
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
        description = "Contest does not exist. Codes: CONTEST_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description = "The contest has started. Codes: CONTEST_ALREADY_LIVE",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.CONTEST_ALREADY_LIVE,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.CONTEST_ALREADY_LIVE)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid contest id. Codes: INVALID_CONTEST_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = ContestApiExamples.INVALID_CONTEST_ID,
                      ref = ApiExamples.REF_PREFIX + ContestApiExamples.INVALID_CONTEST_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "Contest id (UUID version " + Id.VERSION + ")",
              example = "01936f80-1a2b-7c3d-8e4f-5a6b7c8d9e0f")
          @PathVariable("ID")
          UUID id) {
    this.deleteContestSecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

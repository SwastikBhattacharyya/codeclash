package com.codeclash.authservice.session.adapter.inbound.rest;

import com.codeclash.authservice.session.adapter.inbound.rest.dto.CreateSessionRequestBody;
import com.codeclash.authservice.session.adapter.inbound.rest.dto.SessionResponseBody;
import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.authservice.session.model.request.QuerySessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.CreateSessionUseCase;
import com.codeclash.authservice.session.port.inbound.DeleteSessionSecuredUseCase;
import com.codeclash.authservice.session.port.inbound.GetSessionSecuredUseCase;
import com.codeclash.authservice.session.port.inbound.QuerySessionSecuredUseCase;
import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.page.Page;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.page.Size;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.sort.Direction;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.BearerSessionFilter;
import com.codeclash.contracts.auth.session.rest.AuthApiExamples;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Sessions",
    description = "Login sessions; a session id is the bearer token")
@RestController
@RequestMapping(path = "/api/v{version}/sessions", version = "1+")
@RequiredArgsConstructor
public class SessionRestController {

  private final CreateSessionUseCase createSessionUseCase;
  private final QuerySessionSecuredUseCase querySessionSecuredUseCase;
  private final GetSessionSecuredUseCase getSessionSecuredUseCase;
  private final DeleteSessionSecuredUseCase deleteSessionSecuredUseCase;

  @Operation(
      summary = "List sessions",
      description =
          "Pages through sessions matching every given filter. Requires a session. With the"
              + " "
              + AuthPermissions.SESSION_READ
              + " permission any sessions can be listed; otherwise user_id must be given, must "
              + "contain only your own id, and you need "
              + AuthPermissions.SESSION_READ_OWN
              + ".")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching sessions"),
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
                + "properties.permissions names the missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_READ,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_READ),
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_READ_OWN,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_READ_OWN)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_SESSION_ID, "
                + "NULL_SESSION_ID, INVALID_USER_ID, NULL_USER_ID, PAGE_TOO_SMALL, "
                + "SIZE_TOO_SMALL, SIZE_TOO_LARGE, UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.INVALID_SESSION_ID,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.INVALID_SESSION_ID),
                  @ExampleObject(
                      name = SessionApiExamples.NULL_SESSION_ID,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.NULL_SESSION_ID),
                  @ExampleObject(
                      name = SessionApiExamples.INVALID_USER_ID,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.INVALID_USER_ID),
                  @ExampleObject(
                      name = SessionApiExamples.NULL_USER_ID,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.NULL_USER_ID),
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
                      name = SessionApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.UNKNOWN_SORT_FIELD),
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
  ResponseEntity<PagedEntity<SessionResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only sessions with one of these ids; repeat for several",
              example = "01936f70-9e0f-7a1b-8c2d-3e4f5a6b7c8d")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "User IDs",
              description =
                  "Only sessions of one of these users; required and limited to your own id"
                      + " without the "
                      + AuthPermissions.SESSION_READ
                      + " permission; repeat for several",
              example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
          @RequestParam(name = "user_id", required = false, defaultValue = "")
          List<UUID> userIds,
      @Parameter(
              name = "Last Used From",
              description = "Only sessions last used at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "last_used_at_0", required = false)
          Instant minLastUsedAt,
      @Parameter(
              name = "Last Used Until",
              description = "Only sessions last used at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "last_used_at_1", required = false)
          Instant maxLastUsedAt,
      @Parameter(
              name = "Created From",
              description = "Only sessions created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only sessions created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only sessions last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only sessions last changed at or before this instant",
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
              description = "Maximum number of sessions per page",
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
                      allowableValues = {"id", "last_used_at", "created_at", "updated_at"},
                      defaultValue = "id"))
          @RequestParam(name = "sort", defaultValue = "id")
          String sort,
      @Parameter(
              name = "Sort Direction",
              description = "Sort direction",
              schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
          @RequestParam(name = "direction", defaultValue = "asc")
          String direction) {
    QuerySessionRequest request =
        QuerySessionRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .userIds(userIds.stream().map(UserId::of).toList())
            .minLastUsedAt(minLastUsedAt)
            .maxLastUsedAt(maxLastUsedAt)
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<SessionResponseBody> sessions =
        this.querySessionSecuredUseCase.query(request).map(SessionResponseBody::from);
    return ResponseEntity.ok(sessions);
  }

  @Operation(
      summary = "Get a session",
      description =
          "Returns one session by id. Requires a session: reading your own session needs "
              + AuthPermissions.SESSION_READ_OWN
              + ", anyone else's needs "
              + AuthPermissions.SESSION_READ
              + ".")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The session"),
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
                + "properties.permissions names the missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_READ_EITHER,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_READ_EITHER),
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_READ,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_READ),
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_READ_OWN,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_READ_OWN)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Session does not exist. Codes: SESSION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.SESSION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.SESSION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid session id. Codes: INVALID_SESSION_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.INVALID_SESSION_ID,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.INVALID_SESSION_ID)
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
  ResponseEntity<SessionResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Session id (UUID version " + Id.VERSION + ")",
              example = "01936f70-9e0f-7a1b-8c2d-3e4f5a6b7c8d")
          @PathVariable("ID")
          UUID id) {
    AuthorizedSession session = this.getSessionSecuredUseCase.findById(Id.of(id));
    return ResponseEntity.ok(SessionResponseBody.from(session));
  }

  @Operation(
      summary = "Log in",
      description =
          "Checks the credentials and opens a session. The returned id is the bearer token "
              + "for every other request; sessions do not expire and last until deleted. No "
              + "session is needed.")
  @SecurityRequirements
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Session created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created session",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/sessions/01936f70-9e0f-7a1b-8c2d-3e4f5a6b7c8d"))),
    @ApiResponse(
        responseCode = "401",
        description = "Email or password is incorrect. Codes: INVALID_CREDENTIALS",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.INVALID_CREDENTIALS,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.INVALID_CREDENTIALS)
                })),
    @ApiResponse(
        responseCode = "409",
        description =
            "The credentials are correct but the user has not verified their email. Codes: "
                + "UNVERIFIED_USER",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.UNVERIFIED_USER,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.UNVERIFIED_USER)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Password, or both email and username, is missing, or both email and username"
                + " were sent. Codes: NULL_CREDENTIALS, AMBIGUOUS_CREDENTIALS",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.NULL_CREDENTIALS,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.NULL_CREDENTIALS),
                  @ExampleObject(
                      name = SessionApiExamples.AMBIGUOUS_CREDENTIALS,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.AMBIGUOUS_CREDENTIALS)
                }))
  })
  @PostMapping
  ResponseEntity<SessionResponseBody> create(@RequestBody CreateSessionRequestBody body) {
    AuthorizedSession session = this.createSessionUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(session.session().id().value())
            .toUri();
    return ResponseEntity.created(location).body(SessionResponseBody.from(session));
  }

  @Operation(
      summary = "Log out a session",
      description =
          "Deletes a session. Requires a session: deleting your own session needs "
              + AuthPermissions.SESSION_WRITE_OWN
              + ", anyone else's needs "
              + AuthPermissions.SESSION_WRITE
              + ".")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Session deleted"),
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
                + "properties.permissions names the missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_WRITE_EITHER,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_WRITE_EITHER),
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = SessionApiExamples.FORBIDDEN_WRITE_OWN,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.FORBIDDEN_WRITE_OWN)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Session does not exist. Codes: SESSION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.SESSION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.SESSION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid session id. Codes: INVALID_SESSION_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = SessionApiExamples.INVALID_SESSION_ID,
                      ref = ApiExamples.REF_PREFIX + SessionApiExamples.INVALID_SESSION_ID)
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
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "Session id (UUID version " + Id.VERSION + ")",
              example = "01936f70-9e0f-7a1b-8c2d-3e4f5a6b7c8d")
          @PathVariable("ID")
          UUID id) {
    this.deleteSessionSecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

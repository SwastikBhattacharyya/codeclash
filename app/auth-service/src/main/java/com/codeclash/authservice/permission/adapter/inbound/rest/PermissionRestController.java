package com.codeclash.authservice.permission.adapter.inbound.rest;

import com.codeclash.authservice.permission.adapter.inbound.rest.dto.PermissionResponseBody;
import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.model.request.QueryPermissionRequest;
import com.codeclash.authservice.permission.port.inbound.GetPermissionSecuredUseCase;
import com.codeclash.authservice.permission.port.inbound.QueryPermissionSecuredUseCase;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Permissions",
    description = "Permissions that roles grant; read-only, synced from the services at startup")
@RestController
@RequestMapping(path = "/api/v{version}/permissions", version = "1+")
@RequiredArgsConstructor
public class PermissionRestController {

  private final QueryPermissionSecuredUseCase queryPermissionSecuredUseCase;
  private final GetPermissionSecuredUseCase getPermissionSecuredUseCase;

  @Operation(
      summary = "List permissions",
      description =
          "Pages through permissions matching every given filter. Requires the "
              + AuthPermissions.PERMISSION_READ
              + " permission. Permissions are defined by the services and synced at startup; they"
              + " cannot be changed through the API.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching permissions"),
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
                      name = PermissionApiExamples.FORBIDDEN_READ,
                      ref = ApiExamples.REF_PREFIX + PermissionApiExamples.FORBIDDEN_READ)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_PERMISSION_ID, "
                + "NULL_PERMISSION_ID, PAGE_TOO_SMALL, SIZE_TOO_SMALL, SIZE_TOO_LARGE, "
                + "UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = PermissionApiExamples.INVALID_PERMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + PermissionApiExamples.INVALID_PERMISSION_ID),
                  @ExampleObject(
                      name = PermissionApiExamples.NULL_PERMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + PermissionApiExamples.NULL_PERMISSION_ID),
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
                      name = PermissionApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + PermissionApiExamples.UNKNOWN_SORT_FIELD),
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
  ResponseEntity<PagedEntity<PermissionResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only permissions with one of these ids; repeat for several",
              example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Name",
              description =
                  "Only permissions whose name contains one of these terms, ignoring case; "
                      + "repeat for several",
              example = "problem")
          @RequestParam(name = "name", required = false, defaultValue = "")
          List<String> names,
      @Parameter(
              name = "Role IDs",
              description =
                  "Only permissions held by at least one of these roles; ids are not "
                      + "validated, so unknown ids match nothing; repeat for several",
              example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
          @RequestParam(name = "role_id", required = false, defaultValue = "")
          List<UUID> roleIds,
      @Parameter(
              name = "Created From",
              description = "Only permissions created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only permissions created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only permissions last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only permissions last changed at or before this instant",
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
              description = "Maximum number of permissions per page",
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
                      allowableValues = {"id", "name", "created_at", "updated_at"},
                      defaultValue = "id"))
          @RequestParam(name = "sort", defaultValue = "id")
          String sort,
      @Parameter(
              name = "Sort Direction",
              description = "Sort direction",
              schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
          @RequestParam(name = "direction", defaultValue = "asc")
          String direction) {
    QueryPermissionRequest request =
        QueryPermissionRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .names(names)
            .roleIds(roleIds)
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<PermissionResponseBody> permissions =
        this.queryPermissionSecuredUseCase.query(request).map(PermissionResponseBody::from);
    return ResponseEntity.ok(permissions);
  }

  @Operation(
      summary = "Get a permission",
      description =
          "Returns one permission by id. Requires the "
              + AuthPermissions.PERMISSION_READ
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The permission"),
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
                      name = PermissionApiExamples.FORBIDDEN_READ,
                      ref = ApiExamples.REF_PREFIX + PermissionApiExamples.FORBIDDEN_READ)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Permission does not exist. Codes: PERMISSION_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = PermissionApiExamples.PERMISSION_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + PermissionApiExamples.PERMISSION_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid permission id. Codes: INVALID_PERMISSION_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = PermissionApiExamples.INVALID_PERMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + PermissionApiExamples.INVALID_PERMISSION_ID)
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
  ResponseEntity<PermissionResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Permission id (UUID version " + Id.VERSION + ")",
              example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b")
          @PathVariable("ID")
          UUID id) {
    Permission permission = this.getPermissionSecuredUseCase.findById(Id.of(id));
    return ResponseEntity.ok(PermissionResponseBody.from(permission));
  }
}

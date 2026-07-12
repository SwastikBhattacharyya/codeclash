package com.codeclash.authservice.role.adapter.inbound.rest;

import com.codeclash.authservice.role.adapter.inbound.rest.dto.CreateRoleRequestBody;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.PatchRoleRequestBody;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.ReplaceRoleRequestBody;
import com.codeclash.authservice.role.adapter.inbound.rest.dto.RoleResponseBody;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.PermissionId;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.QueryRoleRequest;
import com.codeclash.authservice.role.model.result.ReplaceRoleResult;
import com.codeclash.authservice.role.port.inbound.CreateRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.DeleteRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.GetRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.PatchRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.QueryRoleSecuredUseCase;
import com.codeclash.authservice.role.port.inbound.ReplaceRoleSecuredUseCase;
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
    name = "Roles",
    description = "Named sets of permissions assigned to users")
@RestController
@RequestMapping(path = "/api/v{version}/roles", version = "1+")
@RequiredArgsConstructor
public class RoleRestController {

  private final QueryRoleSecuredUseCase queryRoleSecuredUseCase;
  private final GetRoleSecuredUseCase getRoleSecuredUseCase;
  private final CreateRoleSecuredUseCase createRoleSecuredUseCase;
  private final ReplaceRoleSecuredUseCase replaceRoleSecuredUseCase;
  private final PatchRoleSecuredUseCase patchRoleSecuredUseCase;
  private final DeleteRoleSecuredUseCase deleteRoleSecuredUseCase;

  @Operation(
      summary = "List roles",
      description =
          "Pages through roles matching every given filter. Requires the "
              + AuthPermissions.ROLE_READ
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching roles"),
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
                      name = RoleApiExamples.FORBIDDEN_READ,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.FORBIDDEN_READ)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_ROLE_ID, NULL_ROLE_ID, "
                + "INVALID_PERMISSION_ID, NULL_PERMISSIONS, PAGE_TOO_SMALL, SIZE_TOO_SMALL, "
                + "SIZE_TOO_LARGE, UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_ROLE_ID),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_ROLE_ID),
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_PERMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_PERMISSION_ID),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_PERMISSIONS),
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
                      name = RoleApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.UNKNOWN_SORT_FIELD),
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
  ResponseEntity<PagedEntity<RoleResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only roles with one of these ids; repeat for several",
              example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Name",
              description =
                  "Only roles whose name contains one of these terms, ignoring case; repeat"
                      + " for several",
              example = "manager")
          @RequestParam(name = "name", required = false, defaultValue = "")
          List<String> names,
      @Parameter(
              name = "Permission IDs",
              description =
                  "Only roles granting at least one of these permissions; repeat for "
                      + "several",
              example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b")
          @RequestParam(name = "permission_id", required = false, defaultValue = "")
          List<UUID> permissionIds,
      @Parameter(
              name = "Default",
              description = "Only default (true) or non-default (false) roles",
              example = "false")
          @RequestParam(name = "default", required = false)
          Boolean isDefault,
      @Parameter(
              name = "Created From",
              description = "Only roles created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only roles created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only roles last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only roles last changed at or before this instant",
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
              description = "Maximum number of roles per page",
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
                      allowableValues = {"id", "name", "is_default", "created_at", "updated_at"},
                      defaultValue = "id"))
          @RequestParam(name = "sort", defaultValue = "id")
          String sort,
      @Parameter(
              name = "Sort Direction",
              description = "Sort direction",
              schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "asc"))
          @RequestParam(name = "direction", defaultValue = "asc")
          String direction) {
    QueryRoleRequest request =
        QueryRoleRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .names(names)
            .permissionIds(permissionIds.stream().map(PermissionId::of).toList())
            .isDefault(isDefault)
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<RoleResponseBody> roles =
        this.queryRoleSecuredUseCase.query(request).map(RoleResponseBody::from);
    return ResponseEntity.ok(roles);
  }

  @Operation(
      summary = "Get a role",
      description =
          "Returns one role by id. Requires the "
              + AuthPermissions.ROLE_READ
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The role"),
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
                      name = RoleApiExamples.FORBIDDEN_READ,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.FORBIDDEN_READ)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "Role does not exist. Codes: ROLE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.ROLE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.ROLE_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid role id. Codes: INVALID_ROLE_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_ROLE_ID)
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
  ResponseEntity<RoleResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Role id (UUID version " + Id.VERSION + ")",
              example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
          @PathVariable("ID")
          UUID id) {
    Role<Unresolved> role = this.getRoleSecuredUseCase.findById(Id.of(id));
    return ResponseEntity.ok(RoleResponseBody.from(role));
  }

  @Operation(
      summary = "Create a role",
      description =
          "Creates a non-default role with the given permissions. Requires the "
              + AuthPermissions.ROLE_WRITE
              + " permission.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Role created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created role",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/roles/01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c"))),
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
                      name = RoleApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.FORBIDDEN_WRITE)
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
        description = "Another role already has this name. Codes: DUPLICATE_ROLE_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.DUPLICATE_ROLE_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DUPLICATE_ROLE_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid field or unknown permission. Codes: NULL_NAME, NAME_TOO_LONG, "
                + "INVALID_NAME, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG, "
                + "NULL_PERMISSIONS, INVALID_PERMISSION_ID, PERMISSIONS_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = RoleApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = RoleApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = RoleApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_PERMISSIONS),
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_PERMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_PERMISSION_ID),
                  @ExampleObject(
                      name = RoleApiExamples.PERMISSIONS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.PERMISSIONS_NOT_FOUND)
                }))
  })
  @PostMapping
  ResponseEntity<RoleResponseBody> create(@RequestBody CreateRoleRequestBody body) {
    Role<Unresolved> role = this.createRoleSecuredUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(role.id().value())
            .toUri();
    return ResponseEntity.created(location).body(RoleResponseBody.from(role));
  }

  @Operation(
      summary = "Replace a role",
      description =
          "Replaces every field of the role, or creates it under the given id when it does "
              + "not exist. Requires the "
              + AuthPermissions.ROLE_WRITE
              + " permission. Default roles cannot be changed or deleted.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Role replaced"),
    @ApiResponse(
        responseCode = "201",
        description = "Role created under the given id",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created role",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/roles/01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c"))),
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
                      name = RoleApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.FORBIDDEN_WRITE)
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
            "The role is a default role, or another role already has this name. Codes: "
                + "DEFAULT_ROLE_MODIFICATION, DUPLICATE_ROLE_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.DEFAULT_ROLE_MODIFICATION,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DEFAULT_ROLE_MODIFICATION),
                  @ExampleObject(
                      name = RoleApiExamples.DUPLICATE_ROLE_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DUPLICATE_ROLE_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field or unknown permission. Codes: INVALID_ROLE_ID, NULL_NAME, "
                + "NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, "
                + "DESCRIPTION_TOO_LONG, NULL_PERMISSIONS, INVALID_PERMISSION_ID, "
                + "PERMISSIONS_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_ROLE_ID),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = RoleApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = RoleApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = RoleApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_PERMISSIONS),
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_PERMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_PERMISSION_ID),
                  @ExampleObject(
                      name = RoleApiExamples.PERMISSIONS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.PERMISSIONS_NOT_FOUND)
                }))
  })
  @PutMapping("{ID}")
  ResponseEntity<RoleResponseBody> replace(
      @Parameter(
              name = "ID",
              description = "Role id (UUID version " + Id.VERSION + ")",
              example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
          @PathVariable("ID")
          UUID id,
      @RequestBody ReplaceRoleRequestBody body) {
    ReplaceRoleResult<Unresolved> result =
        this.replaceRoleSecuredUseCase.replace(body.toRequest(id));
    RoleResponseBody response = RoleResponseBody.from(result.role());
    if (!result.created()) return ResponseEntity.ok(response);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build(true).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Update part of a role",
      description =
          "Changes only the fields present in the body; sending permissions replaces the "
              + "whole set. Requires the "
              + AuthPermissions.ROLE_WRITE
              + " permission. Default roles cannot be changed or deleted.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Role updated"),
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
                      name = RoleApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.FORBIDDEN_WRITE)
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
        description = "Role does not exist. Codes: ROLE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.ROLE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.ROLE_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description =
            "The role is a default role, or another role already has this name. Codes: "
                + "DEFAULT_ROLE_MODIFICATION, DUPLICATE_ROLE_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.DEFAULT_ROLE_MODIFICATION,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DEFAULT_ROLE_MODIFICATION),
                  @ExampleObject(
                      name = RoleApiExamples.DUPLICATE_ROLE_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DUPLICATE_ROLE_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field or unknown permission. Codes: INVALID_ROLE_ID, NULL_NAME, "
                + "NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, "
                + "DESCRIPTION_TOO_LONG, NULL_PERMISSIONS, INVALID_PERMISSION_ID, "
                + "PERMISSIONS_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_ROLE_ID),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = RoleApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = RoleApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = RoleApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DESCRIPTION_TOO_LONG),
                  @ExampleObject(
                      name = RoleApiExamples.NULL_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.NULL_PERMISSIONS),
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_PERMISSION_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_PERMISSION_ID),
                  @ExampleObject(
                      name = RoleApiExamples.PERMISSIONS_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.PERMISSIONS_NOT_FOUND)
                }))
  })
  @PatchMapping("{ID}")
  ResponseEntity<RoleResponseBody> patch(
      @Parameter(
              name = "ID",
              description = "Role id (UUID version " + Id.VERSION + ")",
              example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
          @PathVariable("ID")
          UUID id,
      @RequestBody PatchRoleRequestBody body) {
    Role<Unresolved> role = this.patchRoleSecuredUseCase.patch(body.toRequest(id));
    return ResponseEntity.ok(RoleResponseBody.from(role));
  }

  @Operation(
      summary = "Delete a role",
      description =
          "Requires the "
              + AuthPermissions.ROLE_WRITE
              + " permission. Default roles cannot be changed or deleted. Users holding the role "
              + "are moved to the default user role shortly after.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Role deleted"),
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
                      name = RoleApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.FORBIDDEN_WRITE)
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
        description = "Role does not exist. Codes: ROLE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.ROLE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.ROLE_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description = "The role is a default role. Codes: DEFAULT_ROLE_MODIFICATION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.DEFAULT_ROLE_MODIFICATION,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.DEFAULT_ROLE_MODIFICATION)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid role id. Codes: INVALID_ROLE_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = RoleApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + RoleApiExamples.INVALID_ROLE_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "Role id (UUID version " + Id.VERSION + ")",
              example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
          @PathVariable("ID")
          UUID id) {
    this.deleteRoleSecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

package com.codeclash.authservice.user.adapter.inbound.rest;

import com.codeclash.authservice.user.adapter.inbound.rest.dto.CreateUserRequestBody;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.PatchUserRequestBody;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.ReplaceUserRequestBody;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.UserResponseBody;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.request.QueryUserRequest;
import com.codeclash.authservice.user.model.result.ReplaceUserResult;
import com.codeclash.authservice.user.port.inbound.CreateUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.DeleteUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.GetUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.PatchUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.QueryUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.ReplaceUserSecuredUseCase;
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
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Users",
    description = "Accounts, sign-up and email verification")
@RestController
@RequestMapping(path = "/api/v{version}/users", version = "1+")
@RequiredArgsConstructor
public class UserRestController {

  private static final String VERIFICATION_TOKEN_HEADER = "X-EMAIL-VERIFICATION-TOKEN";

  private final QueryUserSecuredUseCase queryUserSecuredUseCase;
  private final GetUserSecuredUseCase getUserSecuredUseCase;
  private final CreateUserSecuredUseCase createUserSecuredUseCase;
  private final ReplaceUserSecuredUseCase replaceUserSecuredUseCase;
  private final PatchUserSecuredUseCase patchUserSecuredUseCase;
  private final DeleteUserSecuredUseCase deleteUserSecuredUseCase;

  @Operation(
      summary = "List users",
      description =
          "Pages through users matching every given filter. A session is optional. "
              + "Filtering by email or sorting by email needs "
              + AuthPermissions.USER_READ_EMAIL
              + ", and filtering by role needs "
              + AuthPermissions.USER_READ_ROLE
              + ". Email and roleId are null unless the caller has "
              + AuthPermissions.USER_READ_EMAIL
              + " / "
              + AuthPermissions.USER_READ_ROLE
              + " (the _own variants for themselves).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching users"),
    @ApiResponse(
        responseCode = "403",
        description =
            "The caller (anonymous included) lacks the permission for an email filter, email"
                + " sort or role filter. Codes: FORBIDDEN; properties.permissions names the"
                + " missing permissions",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_READ_FIELDS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_READ_FIELDS)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_USER_ID, NULL_USER_ID, "
                + "INVALID_ROLE_ID, NULL_ROLE, PAGE_TOO_SMALL, SIZE_TOO_SMALL, SIZE_TOO_LARGE, "
                + "UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USER_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USER_ID),
                  @ExampleObject(
                      name = UserApiExamples.NULL_USER_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_USER_ID),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_ROLE_ID),
                  @ExampleObject(
                      name = UserApiExamples.NULL_ROLE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_ROLE),
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
                      name = UserApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.UNKNOWN_SORT_FIELD),
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
  ResponseEntity<PagedEntity<UserResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only users with one of these ids; repeat for several",
              example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Name",
              description =
                  "Only users whose name contains one of these terms, ignoring case; repeat"
                      + " for several",
              example = "ada")
          @RequestParam(name = "name", required = false, defaultValue = "")
          List<String> names,
      @Parameter(
              name = "Username",
              description =
                  "Only users whose username contains one of these terms, ignoring case; repeat"
                      + " for several",
              example = "ada")
          @RequestParam(name = "username", required = false, defaultValue = "")
          List<String> usernames,
      @Parameter(
              name = "Email",
              description =
                  "Only users whose email contains one of these terms, ignoring case; needs"
                      + " the "
                      + AuthPermissions.USER_READ_EMAIL
                      + " permission; repeat for several",
              example = "example.com")
          @RequestParam(name = "email", required = false, defaultValue = "")
          List<String> emails,
      @Parameter(
              name = "Role IDs",
              description =
                  "Only users with one of these roles; needs the "
                      + AuthPermissions.USER_READ_ROLE
                      + " permission; repeat for several",
              example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
          @RequestParam(name = "role_id", required = false, defaultValue = "")
          List<UUID> roleIds,
      @Parameter(
              name = "Verified",
              description = "Only verified (true) or unverified (false) users",
              example = "true")
          @RequestParam(name = "verified", required = false, defaultValue = "")
          Boolean verified,
      @Parameter(
              name = "Created From",
              description = "Only users created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only users created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only users last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only users last changed at or before this instant",
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
              description = "Maximum number of users per page",
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
                        "id", "name", "username", "email", "verified", "created_at", "updated_at"
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
    QueryUserRequest request =
        QueryUserRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .names(names)
            .usernames(usernames)
            .emails(emails)
            .roleIds(roleIds.stream().map(RoleId::of).toList())
            .verified(verified)
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<UserResponseBody> users =
        this.queryUserSecuredUseCase.query(request).map(UserResponseBody::from);
    return ResponseEntity.ok(users);
  }

  @Operation(
      summary = "Get a user",
      description =
          "Returns one user by id. A session is optional. Email and roleId are null unless "
              + "the caller has "
              + AuthPermissions.USER_READ_EMAIL
              + " / "
              + AuthPermissions.USER_READ_ROLE
              + " (the _own variants for themselves).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The user"),
    @ApiResponse(
        responseCode = "404",
        description = "User does not exist. Codes: USER_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.USER_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USER_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid user id. Codes: INVALID_USER_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USER_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USER_ID)
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
  ResponseEntity<UserResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "User id (UUID version " + Id.VERSION + ")",
              example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
          @PathVariable("ID")
          UUID id) {
    User user = this.getUserSecuredUseCase.findById(Id.of(id));
    return ResponseEntity.ok(UserResponseBody.from(user));
  }

  @Operation(
      summary = "Sign up",
      description =
          "Creates a user; no session is needed. The user starts unverified with the "
              + "default user role unless verified or roleId say otherwise, which needs "
              + AuthPermissions.USER_WRITE_IS_VERIFIED
              + " or "
              + AuthPermissions.USER_WRITE_ROLE
              + " plus every permission of that role. The response is masked like any read, so an"
              + " anonymous caller sees email and roleId as null.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "User created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created user",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/users/01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e"))),
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
                      name = UserApiExamples.FORBIDDEN_FIELDS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_FIELDS),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS)
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
            "Another user already has this email (ignoring case) or username (case-sensitive)."
                + " Codes: DUPLICATE_EMAIL, DUPLICATE_USERNAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.DUPLICATE_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.DUPLICATE_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.DUPLICATE_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.DUPLICATE_USERNAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid field or unknown role. Codes: NULL_NAME, NAME_TOO_SHORT, "
                + "NAME_TOO_LONG, INVALID_NAME, NULL_USERNAME, USERNAME_TOO_SHORT, "
                + "USERNAME_TOO_LONG, INVALID_USERNAME, NULL_EMAIL, EMAIL_TOO_LONG, INVALID_EMAIL, "
                + "NULL_PASSWORD, PASSWORD_TOO_SHORT, PASSWORD_TOO_LONG, "
                + "PASSWORD_MISSING_LOWERCASE, PASSWORD_MISSING_UPPERCASE, "
                + "PASSWORD_MISSING_DIGIT, PASSWORD_MISSING_SPECIAL_CHARACTER, INVALID_ROLE_ID, "
                + "ROLE_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = UserApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = UserApiExamples.NULL_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_USERNAME),
                  @ExampleObject(
                      name = UserApiExamples.USERNAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USERNAME_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.USERNAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USERNAME_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USERNAME),
                  @ExampleObject(
                      name = UserApiExamples.NULL_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.EMAIL_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.EMAIL_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.NULL_PASSWORD,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_PASSWORD),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_LOWERCASE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_LOWERCASE),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_UPPERCASE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_UPPERCASE),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_DIGIT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_DIGIT),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_SPECIAL_CHARACTER,
                      ref =
                          ApiExamples.REF_PREFIX
                              + UserApiExamples.PASSWORD_MISSING_SPECIAL_CHARACTER),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_ROLE_ID),
                  @ExampleObject(
                      name = UserApiExamples.ROLE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.ROLE_NOT_FOUND)
                }))
  })
  @PostMapping
  ResponseEntity<UserResponseBody> create(@RequestBody CreateUserRequestBody body) {
    User user = this.createUserSecuredUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(user.id().value())
            .toUri();
    return ResponseEntity.created(location).body(UserResponseBody.from(user));
  }

  @Operation(
      summary = "Replace a user",
      description =
          "Replaces every field of the user, or creates it under the given id when it does "
              + "not exist; omitted verified and roleId reset to false and the default user role."
              + " Requires a session. Writing yourself needs "
              + AuthPermissions.USER_WRITE_OWN
              + "; anyone else needs "
              + AuthPermissions.USER_WRITE
              + " plus every permission of the target user's current role. Changing verified needs "
              + AuthPermissions.USER_WRITE_IS_VERIFIED
              + ", changing the username needs "
              + AuthPermissions.USER_WRITE_USERNAME
              + " and changing the role needs "
              + AuthPermissions.USER_WRITE_ROLE
              + " plus every permission of the new role (the _own variants for yourself); "
              + "all missing field and role permissions are listed together. Sending the email"
              + " verification token header lets verified go from false to true without the"
              + " verified permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User replaced"),
    @ApiResponse(
        responseCode = "201",
        description = "User created under the given id",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created user",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/users/01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e"))),
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
                      name = UserApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_WRITE_OWN,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_WRITE_OWN),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_FIELDS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_FIELDS),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_FIELDS_OWN,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_FIELDS_OWN),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS)
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
            "Another user already has this email (ignoring case) or username (case-sensitive)."
                + " Codes: DUPLICATE_EMAIL, DUPLICATE_USERNAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.DUPLICATE_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.DUPLICATE_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.DUPLICATE_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.DUPLICATE_USERNAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field, token or unknown role. Codes: INVALID_USER_ID, NULL_NAME, "
                + "NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_USERNAME, USERNAME_TOO_SHORT, "
                + "USERNAME_TOO_LONG, INVALID_USERNAME, NULL_EMAIL, EMAIL_TOO_LONG, "
                + "INVALID_EMAIL, NULL_PASSWORD, PASSWORD_TOO_SHORT, PASSWORD_TOO_LONG, "
                + "PASSWORD_MISSING_LOWERCASE, PASSWORD_MISSING_UPPERCASE, "
                + "PASSWORD_MISSING_DIGIT, PASSWORD_MISSING_SPECIAL_CHARACTER, INVALID_ROLE_ID, "
                + "ROLE_NOT_FOUND, INVALID_VERIFICATION_TOKEN, VERIFICATION_TOKEN_MISMATCH",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USER_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USER_ID),
                  @ExampleObject(
                      name = UserApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = UserApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = UserApiExamples.NULL_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_USERNAME),
                  @ExampleObject(
                      name = UserApiExamples.USERNAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USERNAME_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.USERNAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USERNAME_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USERNAME),
                  @ExampleObject(
                      name = UserApiExamples.NULL_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.EMAIL_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.EMAIL_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.NULL_PASSWORD,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_PASSWORD),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_LOWERCASE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_LOWERCASE),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_UPPERCASE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_UPPERCASE),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_DIGIT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_DIGIT),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_SPECIAL_CHARACTER,
                      ref =
                          ApiExamples.REF_PREFIX
                              + UserApiExamples.PASSWORD_MISSING_SPECIAL_CHARACTER),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_ROLE_ID),
                  @ExampleObject(
                      name = UserApiExamples.ROLE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.ROLE_NOT_FOUND),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_VERIFICATION_TOKEN,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_VERIFICATION_TOKEN),
                  @ExampleObject(
                      name = UserApiExamples.VERIFICATION_TOKEN_MISMATCH,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.VERIFICATION_TOKEN_MISMATCH)
                }))
  })
  @PutMapping("{ID}")
  ResponseEntity<UserResponseBody> replace(
      @Parameter(
              name = "ID",
              description = "User id (UUID version " + Id.VERSION + ")",
              example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
          @PathVariable("ID")
          UUID id,
      @Parameter(
              name = "Email Verification Token",
              in = ParameterIn.HEADER,
              description =
                  "Email verification token (UUID version "
                      + VerificationToken.VERSION
                      + ") sent to the user; lets verified go from false to true without the"
                      + " verified permission",
              example = "3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f")
          @RequestHeader(name = UserRestController.VERIFICATION_TOKEN_HEADER, required = false)
          UUID verificationToken,
      @RequestBody ReplaceUserRequestBody body) {
    ReplaceUserResult result =
        this.replaceUserSecuredUseCase.replace(body.toRequest(id, verificationToken));
    UserResponseBody response = UserResponseBody.from(result.user());
    if (!result.created()) return ResponseEntity.ok(response);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build(true).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Update part of a user",
      description =
          "Changes only the fields present in the body. Without a session, sending only "
              + "{\"verified\": true} with the email verification token header verifies the "
              + "email. Otherwise a session is required. Writing yourself needs "
              + AuthPermissions.USER_WRITE_OWN
              + "; anyone else needs "
              + AuthPermissions.USER_WRITE
              + " plus every permission of the target user's current role. Changing verified needs "
              + AuthPermissions.USER_WRITE_IS_VERIFIED
              + ", changing the username needs "
              + AuthPermissions.USER_WRITE_USERNAME
              + " and changing the role needs "
              + AuthPermissions.USER_WRITE_ROLE
              + " plus every permission of the new role (the _own variants for yourself); "
              + "all missing field and role permissions are listed together.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User updated"),
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
                      name = UserApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_WRITE_OWN,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_WRITE_OWN),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_FIELDS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_FIELDS),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_FIELDS_OWN,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_FIELDS_OWN),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS)
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
        description = "User does not exist. Codes: USER_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.USER_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USER_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description =
            "Another user already has this email (ignoring case) or username (case-sensitive)."
                + " Codes: DUPLICATE_EMAIL, DUPLICATE_USERNAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.DUPLICATE_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.DUPLICATE_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.DUPLICATE_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.DUPLICATE_USERNAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, field, token or unknown role; without a session, a token for an "
                + "unknown or already verified user also gives VERIFICATION_TOKEN_MISMATCH. "
                + "Codes: INVALID_USER_ID, "
                + "NULL_NAME, NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_USERNAME, USERNAME_TOO_SHORT, "
                + "USERNAME_TOO_LONG, INVALID_USERNAME, NULL_EMAIL, "
                + "EMAIL_TOO_LONG, INVALID_EMAIL, NULL_PASSWORD, PASSWORD_TOO_SHORT, "
                + "PASSWORD_TOO_LONG, PASSWORD_MISSING_LOWERCASE, PASSWORD_MISSING_UPPERCASE, "
                + "PASSWORD_MISSING_DIGIT, PASSWORD_MISSING_SPECIAL_CHARACTER, NULL_VERIFIED, "
                + "NULL_ROLE, INVALID_ROLE_ID, ROLE_NOT_FOUND, INVALID_VERIFICATION_TOKEN, "
                + "VERIFICATION_TOKEN_MISMATCH",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USER_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USER_ID),
                  @ExampleObject(
                      name = UserApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = UserApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = UserApiExamples.NULL_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_USERNAME),
                  @ExampleObject(
                      name = UserApiExamples.USERNAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USERNAME_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.USERNAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USERNAME_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USERNAME,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USERNAME),
                  @ExampleObject(
                      name = UserApiExamples.NULL_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.EMAIL_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.EMAIL_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_EMAIL,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_EMAIL),
                  @ExampleObject(
                      name = UserApiExamples.NULL_PASSWORD,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_PASSWORD),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_TOO_SHORT),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_TOO_LONG),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_LOWERCASE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_LOWERCASE),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_UPPERCASE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_UPPERCASE),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_DIGIT,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.PASSWORD_MISSING_DIGIT),
                  @ExampleObject(
                      name = UserApiExamples.PASSWORD_MISSING_SPECIAL_CHARACTER,
                      ref =
                          ApiExamples.REF_PREFIX
                              + UserApiExamples.PASSWORD_MISSING_SPECIAL_CHARACTER),
                  @ExampleObject(
                      name = UserApiExamples.NULL_VERIFIED,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_VERIFIED),
                  @ExampleObject(
                      name = UserApiExamples.NULL_ROLE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.NULL_ROLE),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_ROLE_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_ROLE_ID),
                  @ExampleObject(
                      name = UserApiExamples.ROLE_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.ROLE_NOT_FOUND),
                  @ExampleObject(
                      name = UserApiExamples.INVALID_VERIFICATION_TOKEN,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_VERIFICATION_TOKEN),
                  @ExampleObject(
                      name = UserApiExamples.VERIFICATION_TOKEN_MISMATCH,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.VERIFICATION_TOKEN_MISMATCH)
                }))
  })
  @PatchMapping("{ID}")
  ResponseEntity<UserResponseBody> patch(
      @Parameter(
              name = "ID",
              description = "User id (UUID version " + Id.VERSION + ")",
              example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
          @PathVariable("ID")
          UUID id,
      @Parameter(
              name = "Email Verification Token",
              in = ParameterIn.HEADER,
              description =
                  "Email verification token (UUID version "
                      + VerificationToken.VERSION
                      + ") sent to the user; lets verified go from false to true without the"
                      + " verified permission, and without a session when the body is only"
                      + " {\"verified\": true}",
              example = "3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f")
          @RequestHeader(name = UserRestController.VERIFICATION_TOKEN_HEADER, required = false)
          UUID verificationToken,
      @RequestBody PatchUserRequestBody body) {
    User user = this.patchUserSecuredUseCase.patch(body.toRequest(id, verificationToken));
    return ResponseEntity.ok(UserResponseBody.from(user));
  }

  @Operation(
      summary = "Delete a user",
      description =
          "Deletes the user and all their sessions. Requires a session. Writing yourself "
              + "needs "
              + AuthPermissions.USER_WRITE_OWN
              + "; anyone else needs "
              + AuthPermissions.USER_WRITE
              + " plus every permission of the target user's current role.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "User deleted"),
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
                      name = UserApiExamples.FORBIDDEN_WRITE,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_WRITE),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_WRITE_OWN,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_WRITE_OWN),
                  @ExampleObject(
                      name = UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS)
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
        description = "User does not exist. Codes: USER_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.USER_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.USER_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid user id. Codes: INVALID_USER_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = UserApiExamples.INVALID_USER_ID,
                      ref = ApiExamples.REF_PREFIX + UserApiExamples.INVALID_USER_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "User id (UUID version " + Id.VERSION + ")",
              example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
          @PathVariable("ID")
          UUID id) {
    this.deleteUserSecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

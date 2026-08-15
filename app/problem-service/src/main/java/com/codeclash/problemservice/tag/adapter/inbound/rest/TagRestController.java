package com.codeclash.problemservice.tag.adapter.inbound.rest;

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
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.CreateTagRequestBody;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.PatchTagRequestBody;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.ReplaceTagRequestBody;
import com.codeclash.problemservice.tag.adapter.inbound.rest.dto.TagResponseBody;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.QueryTagRequest;
import com.codeclash.problemservice.tag.model.result.ReplaceTagResult;
import com.codeclash.problemservice.tag.port.inbound.CreateTagSecuredUseCase;
import com.codeclash.problemservice.tag.port.inbound.DeleteTagSecuredUseCase;
import com.codeclash.problemservice.tag.port.inbound.GetTagUseCase;
import com.codeclash.problemservice.tag.port.inbound.PatchTagSecuredUseCase;
import com.codeclash.problemservice.tag.port.inbound.QueryTagUseCase;
import com.codeclash.problemservice.tag.port.inbound.ReplaceTagSecuredUseCase;
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
    name = "Tags",
    description = "Labels that group related questions")
@RestController
@RequestMapping(path = "/api/v{version}/tags", version = "1+")
@RequiredArgsConstructor
public class TagRestController {

  private final QueryTagUseCase queryTagUseCase;
  private final GetTagUseCase getTagUseCase;
  private final CreateTagSecuredUseCase createTagSecuredUseCase;
  private final ReplaceTagSecuredUseCase replaceTagSecuredUseCase;
  private final PatchTagSecuredUseCase patchTagSecuredUseCase;
  private final DeleteTagSecuredUseCase deleteTagSecuredUseCase;

  @Operation(
      summary = "List tags",
      description =
          "Pages through tags matching every given filter. Public; no session needed.")
  @SecurityRequirements
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching tags"),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_TAG_ID, NULL_TAG_ID, "
                + "PAGE_TOO_SMALL, SIZE_TOO_SMALL, SIZE_TOO_LARGE, UNKNOWN_SORT_FIELD, "
                + "INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_TAG_ID),
                  @ExampleObject(
                      name = TagApiExamples.NULL_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NULL_TAG_ID),
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
                      name = TagApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.UNKNOWN_SORT_FIELD),
                  @ExampleObject(
                      name = ApiExamples.INVALID_SORT_DIRECTION,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.INVALID_SORT_DIRECTION)
                }))
  })
  @GetMapping
  ResponseEntity<PagedEntity<TagResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only tags with one of these ids; repeat for several",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Name",
              description =
                  "Only tags whose name contains one of these terms, ignoring case; repeat "
                      + "for several",
              example = "dynamic")
          @RequestParam(name = "name", required = false, defaultValue = "")
          List<String> names,
      @Parameter(
              name = "Description",
              description =
                  "Only tags whose description contains one of these terms, ignoring case;"
                      + " repeat for several",
              example = "programming")
          @RequestParam(name = "description", required = false, defaultValue = "")
          List<String> descriptions,
      @Parameter(
              name = "Created From",
              description = "Only tags created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only tags created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only tags last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only tags last changed at or before this instant",
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
              description = "Maximum number of tags per page",
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
    QueryTagRequest request =
        QueryTagRequest.builder()
            .ids(ids.stream().map(Id::of).toList())
            .names(names)
            .descriptions(descriptions)
            .minCreatedAt(minCreatedAt)
            .maxCreatedAt(maxCreatedAt)
            .minUpdatedAt(minUpdatedAt)
            .maxUpdatedAt(maxUpdatedAt)
            .pagination(new PaginationRequest(new Page(page), new Size(size)))
            .sort(new SortRequest(sort, Direction.from(direction)))
            .build();
    PagedEntity<TagResponseBody> tags =
        this.queryTagUseCase.query(request).map(TagResponseBody::from);
    return ResponseEntity.ok(tags);
  }

  @Operation(
      summary = "Get a tag",
      description = "Returns one tag by id. Public; no session needed.")
  @SecurityRequirements
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The tag"),
    @ApiResponse(
        responseCode = "404",
        description = "Tag does not exist. Codes: TAG_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.TAG_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.TAG_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid tag id. Codes: INVALID_TAG_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_TAG_ID)
                }))
  })
  @GetMapping("{ID}")
  ResponseEntity<TagResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Tag id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id) {
    Tag tag = this.getTagUseCase.findById(Id.of(id));
    return ResponseEntity.ok(TagResponseBody.from(tag));
  }

  @Operation(
      summary = "Create a tag",
      description = "Requires the " + ProblemPermissions.TAG_WRITE + " permission.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Tag created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created tag",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/tags/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f"))),
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
        description = "Session lacks the required permission. Codes: FORBIDDEN",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.FORBIDDEN)
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
        description = "Another tag already has this name, ignoring case. Codes: DUPLICATE_TAG_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.DUPLICATE_TAG_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DUPLICATE_TAG_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid name or description. Codes: NULL_NAME, NAME_TOO_SHORT, NAME_TOO_LONG, "
                + "INVALID_NAME, NULL_DESCRIPTION, DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = TagApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = TagApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = TagApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = TagApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = TagApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = TagApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DESCRIPTION_TOO_LONG)
                }))
  })
  @PostMapping
  ResponseEntity<TagResponseBody> create(@RequestBody CreateTagRequestBody body) {
    Tag tag = this.createTagSecuredUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(tag.id().value())
            .toUri();
    return ResponseEntity.created(location).body(TagResponseBody.from(tag));
  }

  @Operation(
      summary = "Replace a tag",
      description =
          "Replaces every field of the tag, or creates it under the given id when it does not"
              + " exist. Requires the "
              + ProblemPermissions.TAG_WRITE
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tag replaced"),
    @ApiResponse(
        responseCode = "201",
        description = "Tag created under the given id",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created tag",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/tags/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f"))),
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
        description = "Session lacks the required permission. Codes: FORBIDDEN",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.FORBIDDEN)
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
        description = "Another tag already has this name, ignoring case. Codes: DUPLICATE_TAG_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.DUPLICATE_TAG_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DUPLICATE_TAG_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, name or description. Codes: INVALID_TAG_ID, NULL_NAME, "
                + "NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, "
                + "DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_TAG_ID),
                  @ExampleObject(
                      name = TagApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = TagApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = TagApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = TagApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = TagApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = TagApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = TagApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DESCRIPTION_TOO_LONG)
                }))
  })
  @PutMapping("{ID}")
  ResponseEntity<TagResponseBody> replace(
      @Parameter(
              name = "ID",
              description = "Tag id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id,
      @RequestBody ReplaceTagRequestBody body) {
    ReplaceTagResult result = this.replaceTagSecuredUseCase.replace(body.toRequest(id));
    TagResponseBody response = TagResponseBody.from(result.tag());
    if (!result.created()) return ResponseEntity.ok(response);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build(true).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Update part of a tag",
      description =
          "Changes only the fields present in the body. Requires the "
              + ProblemPermissions.TAG_WRITE
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tag updated"),
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
        description = "Session lacks the required permission. Codes: FORBIDDEN",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.FORBIDDEN)
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
        description = "Tag does not exist. Codes: TAG_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.TAG_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.TAG_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description = "Another tag already has this name, ignoring case. Codes: DUPLICATE_TAG_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.DUPLICATE_TAG_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DUPLICATE_TAG_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, name or description. Codes: INVALID_TAG_ID, NULL_NAME, "
                + "NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, "
                + "DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_TAG_ID),
                  @ExampleObject(
                      name = TagApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = TagApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = TagApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = TagApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = TagApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = TagApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = TagApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.DESCRIPTION_TOO_LONG)
                }))
  })
  @PatchMapping("{ID}")
  ResponseEntity<TagResponseBody> patch(
      @Parameter(
              name = "ID",
              description = "Tag id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id,
      @RequestBody PatchTagRequestBody body) {
    Tag tag = this.patchTagSecuredUseCase.patch(body.toRequest(id));
    return ResponseEntity.ok(TagResponseBody.from(tag));
  }

  @Operation(
      summary = "Delete a tag",
      description = "Requires the " + ProblemPermissions.TAG_WRITE + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Tag deleted"),
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
        description = "Session lacks the required permission. Codes: FORBIDDEN",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.FORBIDDEN)
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
        description = "Tag does not exist. Codes: TAG_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.TAG_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.TAG_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid tag id. Codes: INVALID_TAG_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = TagApiExamples.INVALID_TAG_ID,
                      ref = ApiExamples.REF_PREFIX + TagApiExamples.INVALID_TAG_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "Tag id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id) {
    this.deleteTagSecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

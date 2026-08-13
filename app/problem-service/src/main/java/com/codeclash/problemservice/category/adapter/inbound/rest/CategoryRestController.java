package com.codeclash.problemservice.category.adapter.inbound.rest;

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
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.CategoryResponseBody;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.CreateCategoryRequestBody;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.PatchCategoryRequestBody;
import com.codeclash.problemservice.category.adapter.inbound.rest.dto.ReplaceCategoryRequestBody;
import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.request.QueryCategoryRequest;
import com.codeclash.problemservice.category.model.result.ReplaceCategoryResult;
import com.codeclash.problemservice.category.port.inbound.CreateCategorySecuredUseCase;
import com.codeclash.problemservice.category.port.inbound.DeleteCategorySecuredUseCase;
import com.codeclash.problemservice.category.port.inbound.GetCategoryUseCase;
import com.codeclash.problemservice.category.port.inbound.PatchCategorySecuredUseCase;
import com.codeclash.problemservice.category.port.inbound.QueryCategoryUseCase;
import com.codeclash.problemservice.category.port.inbound.ReplaceCategorySecuredUseCase;
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
    name = "Categories",
    description = "Topics that classify questions")
@RestController
@RequestMapping(path = "/api/v{version}/categories", version = "1+")
@RequiredArgsConstructor
public class CategoryRestController {

  private final QueryCategoryUseCase queryCategoryUseCase;
  private final GetCategoryUseCase getCategoryUseCase;
  private final CreateCategorySecuredUseCase createCategorySecuredUseCase;
  private final ReplaceCategorySecuredUseCase replaceCategorySecuredUseCase;
  private final PatchCategorySecuredUseCase patchCategorySecuredUseCase;
  private final DeleteCategorySecuredUseCase deleteCategorySecuredUseCase;

  @Operation(
      summary = "List categories",
      description =
          "Pages through categories matching every given filter. Public; no session needed.")
  @SecurityRequirements
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "One page of matching categories"),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id filter, paging or sorting. Codes: INVALID_CATEGORY_ID, "
                + "NULL_CATEGORY_ID, PAGE_TOO_SMALL, SIZE_TOO_SMALL, SIZE_TOO_LARGE, "
                + "UNKNOWN_SORT_FIELD, INVALID_SORT_DIRECTION",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_CATEGORY_ID),
                  @ExampleObject(
                      name = CategoryApiExamples.NULL_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NULL_CATEGORY_ID),
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
                      name = CategoryApiExamples.UNKNOWN_SORT_FIELD,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.UNKNOWN_SORT_FIELD),
                  @ExampleObject(
                      name = ApiExamples.INVALID_SORT_DIRECTION,
                      ref = ApiExamples.REF_PREFIX + ApiExamples.INVALID_SORT_DIRECTION)
                }))
  })
  @GetMapping
  ResponseEntity<PagedEntity<CategoryResponseBody>> findAll(
      @Parameter(
              name = "IDs",
              description = "Only categories with one of these ids; repeat for several",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @RequestParam(name = "id", required = false, defaultValue = "")
          List<UUID> ids,
      @Parameter(
              name = "Name",
              description =
                  "Only categories whose name contains one of these terms, ignoring case; repeat "
                      + "for several",
              example = "graph")
          @RequestParam(name = "name", required = false, defaultValue = "")
          List<String> names,
      @Parameter(
              name = "Description",
              description =
                  "Only categories whose description contains one of these terms, ignoring case;"
                      + " repeat for several",
              example = "traversal")
          @RequestParam(name = "description", required = false, defaultValue = "")
          List<String> descriptions,
      @Parameter(
              name = "Created From",
              description = "Only categories created at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_0", required = false)
          Instant minCreatedAt,
      @Parameter(
              name = "Created Until",
              description = "Only categories created at or before this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "created_at_1", required = false)
          Instant maxCreatedAt,
      @Parameter(
              name = "Updated From",
              description = "Only categories last changed at or after this instant",
              example = "2026-01-01T12:00:00Z")
          @RequestParam(name = "updated_at_0", required = false)
          Instant minUpdatedAt,
      @Parameter(
              name = "Updated Until",
              description = "Only categories last changed at or before this instant",
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
              description = "Maximum number of categories per page",
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
    QueryCategoryRequest request =
        QueryCategoryRequest.builder()
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
    PagedEntity<CategoryResponseBody> categories =
        this.queryCategoryUseCase.query(request).map(CategoryResponseBody::from);
    return ResponseEntity.ok(categories);
  }

  @Operation(
      summary = "Get a category",
      description = "Returns one category by id. Public; no session needed.")
  @SecurityRequirements
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "The category"),
    @ApiResponse(
        responseCode = "404",
        description = "Category does not exist. Codes: CATEGORY_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.CATEGORY_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.CATEGORY_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid category id. Codes: INVALID_CATEGORY_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_CATEGORY_ID)
                }))
  })
  @GetMapping("{ID}")
  ResponseEntity<CategoryResponseBody> findById(
      @Parameter(
              name = "ID",
              description = "Category id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id) {
    Category category = this.getCategoryUseCase.findById(Id.of(id));
    return ResponseEntity.ok(CategoryResponseBody.from(category));
  }

  @Operation(
      summary = "Create a category",
      description = "Requires the " + ProblemPermissions.CATEGORY_WRITE + " permission.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Category created",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created category",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/categories/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f"))),
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
                      name = CategoryApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.FORBIDDEN)
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
            "Another category already has this name, ignoring case. Codes:"
                + " DUPLICATE_CATEGORY_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.DUPLICATE_CATEGORY_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DUPLICATE_CATEGORY_NAME)
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
                      name = CategoryApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = CategoryApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = CategoryApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = CategoryApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = CategoryApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = CategoryApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DESCRIPTION_TOO_LONG)
                }))
  })
  @PostMapping
  ResponseEntity<CategoryResponseBody> create(@RequestBody CreateCategoryRequestBody body) {
    Category category = this.createCategorySecuredUseCase.create(body.toRequest());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path("/{id}")
            .buildAndExpand(category.id().value())
            .toUri();
    return ResponseEntity.created(location).body(CategoryResponseBody.from(category));
  }

  @Operation(
      summary = "Replace a category",
      description =
          "Replaces every field of the category, or creates it under the given id when it does not"
              + " exist. Requires the "
              + ProblemPermissions.CATEGORY_WRITE
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category replaced"),
    @ApiResponse(
        responseCode = "201",
        description = "Category created under the given id",
        headers =
            @Header(
                name = HttpHeaders.LOCATION,
                description = "URI of the created category",
                schema =
                    @Schema(
                        type = "string",
                        format = "uri",
                        example = "/api/v1/categories/01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f"))),
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
                      name = CategoryApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.FORBIDDEN)
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
            "Another category already has this name, ignoring case. Codes:"
                + " DUPLICATE_CATEGORY_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.DUPLICATE_CATEGORY_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DUPLICATE_CATEGORY_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, name or description. Codes: INVALID_CATEGORY_ID, NULL_NAME, "
                + "NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, "
                + "DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_CATEGORY_ID),
                  @ExampleObject(
                      name = CategoryApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = CategoryApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = CategoryApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = CategoryApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = CategoryApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = CategoryApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DESCRIPTION_TOO_LONG)
                }))
  })
  @PutMapping("{ID}")
  ResponseEntity<CategoryResponseBody> replace(
      @Parameter(
              name = "ID",
              description = "Category id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id,
      @RequestBody ReplaceCategoryRequestBody body) {
    ReplaceCategoryResult result = this.replaceCategorySecuredUseCase.replace(body.toRequest(id));
    CategoryResponseBody response = CategoryResponseBody.from(result.category());
    if (!result.created()) return ResponseEntity.ok(response);
    URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().build(true).toUri();
    return ResponseEntity.created(location).body(response);
  }

  @Operation(
      summary = "Update part of a category",
      description =
          "Changes only the fields present in the body. Requires the "
              + ProblemPermissions.CATEGORY_WRITE
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Category updated"),
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
                      name = CategoryApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.FORBIDDEN)
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
        description = "Category does not exist. Codes: CATEGORY_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.CATEGORY_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.CATEGORY_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "409",
        description =
            "Another category already has this name, ignoring case. Codes:"
                + " DUPLICATE_CATEGORY_NAME",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.DUPLICATE_CATEGORY_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DUPLICATE_CATEGORY_NAME)
                })),
    @ApiResponse(
        responseCode = "422",
        description =
            "Invalid id, name or description. Codes: INVALID_CATEGORY_ID, NULL_NAME, "
                + "NAME_TOO_SHORT, NAME_TOO_LONG, INVALID_NAME, NULL_DESCRIPTION, "
                + "DESCRIPTION_TOO_SHORT, DESCRIPTION_TOO_LONG",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_CATEGORY_ID),
                  @ExampleObject(
                      name = CategoryApiExamples.NULL_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NULL_NAME),
                  @ExampleObject(
                      name = CategoryApiExamples.NAME_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NAME_TOO_SHORT),
                  @ExampleObject(
                      name = CategoryApiExamples.NAME_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NAME_TOO_LONG),
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_NAME,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_NAME),
                  @ExampleObject(
                      name = CategoryApiExamples.NULL_DESCRIPTION,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.NULL_DESCRIPTION),
                  @ExampleObject(
                      name = CategoryApiExamples.DESCRIPTION_TOO_SHORT,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DESCRIPTION_TOO_SHORT),
                  @ExampleObject(
                      name = CategoryApiExamples.DESCRIPTION_TOO_LONG,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.DESCRIPTION_TOO_LONG)
                }))
  })
  @PatchMapping("{ID}")
  ResponseEntity<CategoryResponseBody> patch(
      @Parameter(
              name = "ID",
              description = "Category id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id,
      @RequestBody PatchCategoryRequestBody body) {
    Category category = this.patchCategorySecuredUseCase.patch(body.toRequest(id));
    return ResponseEntity.ok(CategoryResponseBody.from(category));
  }

  @Operation(
      summary = "Delete a category",
      description =
          "Also removes the category from every question that uses it. Requires the "
              + ProblemPermissions.CATEGORY_WRITE
              + " permission.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Category deleted"),
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
                      name = CategoryApiExamples.FORBIDDEN,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.FORBIDDEN)
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
        description = "Category does not exist. Codes: CATEGORY_NOT_FOUND",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.CATEGORY_NOT_FOUND,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.CATEGORY_NOT_FOUND)
                })),
    @ApiResponse(
        responseCode = "422",
        description = "Invalid category id. Codes: INVALID_CATEGORY_ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponseBody.class),
                examples = {
                  @ExampleObject(
                      name = CategoryApiExamples.INVALID_CATEGORY_ID,
                      ref = ApiExamples.REF_PREFIX + CategoryApiExamples.INVALID_CATEGORY_ID)
                }))
  })
  @DeleteMapping("{ID}")
  ResponseEntity<Void> delete(
      @Parameter(
              name = "ID",
              description = "Category id (UUID version " + Id.VERSION + ")",
              example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
          @PathVariable("ID")
          UUID id) {
    this.deleteCategorySecuredUseCase.delete(Id.of(id));
    return ResponseEntity.noContent().build();
  }
}

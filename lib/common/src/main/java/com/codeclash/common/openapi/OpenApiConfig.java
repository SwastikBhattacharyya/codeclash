package com.codeclash.common.openapi;

import com.codeclash.common.exception.CommonErrorCode;
import com.codeclash.common.page.Page;
import com.codeclash.common.page.Size;
import com.codeclash.common.page.exception.PageTooSmallException;
import com.codeclash.common.page.exception.SizeTooLargeException;
import com.codeclash.common.page.exception.SizeTooSmallException;
import com.codeclash.common.rest.ErrorResponseBody;
import com.codeclash.common.rest.GlobalExceptionHandler;
import com.codeclash.common.rest.dto.AllowedValuesProperties;
import com.codeclash.common.rest.dto.MinProperties;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.common.rest.dto.TypeMismatchProperties;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.Direction;
import com.codeclash.common.sort.exception.InvalidSortDirectionException;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenApiProperties.class)
class OpenApiConfig {

  static final String BEARER_SCHEME = "bearer";

  private static final String ERROR_SCHEMA = ErrorResponseBody.class.getSimpleName();
  private static final List<String> SAMPLE_SORT_FIELDS = List.of("id", "name");
  private static final String PATH_PARAMETER = "path";
  private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\{([^}/]+)}");
  private static final String WORD_BOUNDARY = "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])";
  private static final String WORD_SEPARATOR = " ";

  private static ApiResponse errorResponse(String description, List<String> exampleNames) {
    Schema<?> schema =
        new Schema<>().$ref(Components.COMPONENTS_SCHEMAS_REF + OpenApiConfig.ERROR_SCHEMA);
    MediaType mediaType = new MediaType().schema(schema);
    exampleNames.forEach(name -> mediaType.addExamples(name, ApiExamples.ref(name)));
    String codes = String.join(", ", exampleNames.stream().map(ApiExamples::code).toList());
    return new ApiResponse()
        .description(description + ". Codes: " + codes)
        .content(new Content().addMediaType(MimeTypeUtils.APPLICATION_JSON_VALUE, mediaType));
  }

  private static boolean canBeMissing(Parameter parameter) {
    return Boolean.TRUE.equals(parameter.getRequired())
        && !OpenApiConfig.PATH_PARAMETER.equals(parameter.getIn());
  }

  private static boolean isVersioned(HandlerMethod handlerMethod) {
    RequestMapping method =
        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequestMapping.class);
    RequestMapping type =
        AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequestMapping.class);
    return (method != null && !method.version().isEmpty())
        || (type != null && !type.version().isEmpty());
  }

  private static String spaced(String name) {
    return String.join(OpenApiConfig.WORD_SEPARATOR, name.split(OpenApiConfig.WORD_BOUNDARY));
  }

  private static String template(String name) {
    return "{" + name + "}";
  }

  private static void renamePathParameters(List<Parameter> parameters, Map<String, String> names) {
    if (parameters == null) return;
    parameters.stream()
        .filter(parameter -> OpenApiConfig.PATH_PARAMETER.equals(parameter.getIn()))
        .forEach(
            parameter ->
                parameter.setName(names.getOrDefault(parameter.getName(), parameter.getName())));
  }

  @Bean
  OpenAPI openApi(OpenApiProperties properties, BuildProperties buildProperties) {
    return new OpenAPI()
        .info(
            new Info()
                .title(properties.title())
                .description(properties.description())
                .version(buildProperties.getVersion()))
        .components(
            new Components()
                .addSecuritySchemes(
                    OpenApiConfig.BEARER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(OpenApiConfig.BEARER_SCHEME)))
        .addSecurityItem(new SecurityRequirement().addList(OpenApiConfig.BEARER_SCHEME));
  }

  @Bean
  OpenApiCustomizer commonOpenApiExamples(
      SpringDocConfigProperties springDocProperties, Environment environment) {
    return openApi -> {
      Components components = openApi.getComponents();
      ApiSchemas.register(
          springDocProperties,
          components,
          ErrorResponseBody.class,
          AllowedValuesProperties.class,
          MinProperties.class,
          RangeProperties.class,
          TypeMismatchProperties.class,
          UnknownSortFieldProperties.class);
      components
          .addExamples(
              ApiExamples.TYPE_MISMATCH,
              ApiExamples.error(
                  CommonErrorCode.TYPE_MISMATCH,
                  GlobalExceptionHandler.TYPE_MISMATCH_MESSAGE,
                  new TypeMismatchProperties("id", "UUID")))
          .addExamples(
              ApiExamples.INVALID_API_VERSION,
              ApiExamples.error(
                  CommonErrorCode.INVALID_API_VERSION,
                  GlobalExceptionHandler.INVALID_API_VERSION_MESSAGE,
                  new AllowedValuesProperties(
                      GlobalExceptionHandler.allowedApiVersions(environment))))
          .addExamples(
              ApiExamples.MISSING_PARAMETER,
              ApiExamples.error(
                  CommonErrorCode.MISSING_PARAMETER,
                  GlobalExceptionHandler.MISSING_PARAMETER_MESSAGE))
          .addExamples(
              ApiExamples.MALFORMED_REQUEST_BODY,
              ApiExamples.error(
                  CommonErrorCode.MALFORMED_REQUEST_BODY,
                  GlobalExceptionHandler.MALFORMED_REQUEST_BODY_MESSAGE))
          .addExamples(
              ApiExamples.INTERNAL_ERROR,
              ApiExamples.error(
                  CommonErrorCode.INTERNAL_ERROR, GlobalExceptionHandler.INTERNAL_ERROR_MESSAGE))
          .addExamples(
              ApiExamples.PAGE_TOO_SMALL,
              ApiExamples.error(
                  new PageTooSmallException(Page.MIN - 1), new MinProperties(Page.MIN)))
          .addExamples(
              ApiExamples.SIZE_TOO_SMALL,
              ApiExamples.error(
                  new SizeTooSmallException(Size.MIN - 1), new RangeProperties(Size.MIN, Size.MAX)))
          .addExamples(
              ApiExamples.SIZE_TOO_LARGE,
              ApiExamples.error(
                  new SizeTooLargeException(Size.MAX + 1), new RangeProperties(Size.MIN, Size.MAX)))
          .addExamples(
              ApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException("rating", OpenApiConfig.SAMPLE_SORT_FIELDS),
                  new UnknownSortFieldProperties("rating", OpenApiConfig.SAMPLE_SORT_FIELDS)))
          .addExamples(
              ApiExamples.INVALID_SORT_DIRECTION,
              ApiExamples.error(
                  new InvalidSortDirectionException("up"),
                  new AllowedValuesProperties(Direction.allowed())));
    };
  }

  @Bean
  OpenApiCustomizer pathParameterNames() {
    return openApi -> {
      if (openApi.getPaths() == null) return;
      Paths paths = new Paths();
      openApi
          .getPaths()
          .forEach(
              (path, item) -> {
                Map<String, String> names = new LinkedHashMap<>();
                Matcher matcher = OpenApiConfig.TEMPLATE_VARIABLE.matcher(path);
                while (matcher.find())
                  names.put(matcher.group(1), OpenApiConfig.spaced(matcher.group(1)));
                String renamed = path;
                for (Map.Entry<String, String> name : names.entrySet())
                  renamed =
                      renamed.replace(
                          OpenApiConfig.template(name.getKey()),
                          OpenApiConfig.template(name.getValue()));
                OpenApiConfig.renamePathParameters(item.getParameters(), names);
                item.readOperations()
                    .forEach(
                        operation ->
                            OpenApiConfig.renamePathParameters(operation.getParameters(), names));
                paths.addPathItem(renamed, item);
              });
      paths.setExtensions(openApi.getPaths().getExtensions());
      openApi.setPaths(paths);
    };
  }

  @Bean
  OperationCustomizer commonErrorResponses() {
    return (operation, handlerMethod) -> {
      if (operation.getResponses() == null) operation.setResponses(new ApiResponses());
      List<Parameter> parameters =
          operation.getParameters() == null ? List.of() : operation.getParameters();
      List<String> badRequest = new ArrayList<>();
      if (OpenApiConfig.isVersioned(handlerMethod)) badRequest.add(ApiExamples.INVALID_API_VERSION);
      if (!parameters.isEmpty()) badRequest.add(ApiExamples.TYPE_MISMATCH);
      if (parameters.stream().anyMatch(OpenApiConfig::canBeMissing))
        badRequest.add(ApiExamples.MISSING_PARAMETER);
      if (operation.getRequestBody() != null) badRequest.add(ApiExamples.MALFORMED_REQUEST_BODY);
      if (!badRequest.isEmpty())
        operation
            .getResponses()
            .putIfAbsent(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                OpenApiConfig.errorResponse(
                    "The API version is not supported, the request could not be read or a value"
                        + " has the wrong type",
                    badRequest));
      operation
          .getResponses()
          .putIfAbsent(
              String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
              OpenApiConfig.errorResponse(
                  "Unexpected server error", List.of(ApiExamples.INTERNAL_ERROR)));
      return operation;
    };
  }
}

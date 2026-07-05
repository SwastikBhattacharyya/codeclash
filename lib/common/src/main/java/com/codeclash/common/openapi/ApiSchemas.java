package com.codeclash.common.openapi;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.properties.SpringDocConfigProperties;

public final class ApiSchemas {

  private ApiSchemas() {}

  public static void register(
      SpringDocConfigProperties properties, Components components, Class<?>... types) {
    ModelConverters converters = ModelConverters.getInstance(properties.isOpenapi31());
    for (Class<?> type : types) {
      converters.readAll(type).forEach(components::addSchemas);
    }
  }
}

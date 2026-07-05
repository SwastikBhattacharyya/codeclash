package com.codeclash.common.openapi;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.openapi")
@Validated
public record OpenApiProperties(@NotBlank String title, @NotBlank String description) {}

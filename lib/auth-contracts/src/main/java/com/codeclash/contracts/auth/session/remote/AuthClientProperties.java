package com.codeclash.contracts.auth.session.remote;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.auth")
@Validated
public record AuthClientProperties(
    @NotNull URI baseUrl, @NotNull Duration connectTimeout, @NotNull Duration readTimeout) {}

package com.codeclash.notificationservice.user.adapter.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.user.verification-mail")
@Validated
public record UserVerificationMailProperties(@NotBlank @Email String from, @NotBlank String subject) {}

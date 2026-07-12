package com.codeclash.authservice.role.adapter.config;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("codeclash.role")
@Validated
public record RoleProperties(@Name("default") @NotEmpty List<DefaultRoleProperties> defaults) {}

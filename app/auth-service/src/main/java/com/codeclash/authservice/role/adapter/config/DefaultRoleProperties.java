package com.codeclash.authservice.role.adapter.config;

import java.util.List;
import org.springframework.boot.context.properties.bind.DefaultValue;

public record DefaultRoleProperties(
    String name, String description, @DefaultValue List<String> permissions) {}

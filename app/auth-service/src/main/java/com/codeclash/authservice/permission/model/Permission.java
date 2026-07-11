package com.codeclash.authservice.permission.model;

import com.codeclash.common.permission.Description;
import com.codeclash.common.permission.Name;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;

public record Permission(
    Id id, Name name, Description description, CreatedAt createdAt, UpdatedAt updatedAt) {}

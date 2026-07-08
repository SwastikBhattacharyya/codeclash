package com.codeclash.contracts.auth.session.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Permissions that would allow the request")
public record RequiredPermissionsProperties(
    @ArraySchema(
            arraySchema =
                @Schema(
                    description =
                        "Permissions the request is missing; the operation description says"
                            + " whether all or any one is needed",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(example = "problem:tag:write"))
        List<String> permissions) {}

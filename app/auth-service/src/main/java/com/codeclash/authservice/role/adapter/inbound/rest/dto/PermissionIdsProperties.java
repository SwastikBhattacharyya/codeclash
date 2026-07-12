package com.codeclash.authservice.role.adapter.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Submitted permission ids; at least one does not exist")
public record PermissionIdsProperties(
    @ArraySchema(
            arraySchema =
                @Schema(
                    description =
                        "All submitted permission ids; at least one of them does not exist",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b"))
        List<UUID> ids) {}

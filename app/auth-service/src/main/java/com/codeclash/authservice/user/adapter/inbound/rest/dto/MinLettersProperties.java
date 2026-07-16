package com.codeclash.authservice.user.adapter.inbound.rest.dto;

import com.codeclash.authservice.user.model.Name;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Minimum number of ASCII letters the value needs")
public record MinLettersProperties(
    @Schema(
            description = "Smallest accepted number of letters",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "" + Name.MIN_LETTERS)
        int minLetters) {}

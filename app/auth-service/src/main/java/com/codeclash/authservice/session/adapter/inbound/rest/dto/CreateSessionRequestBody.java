package com.codeclash.authservice.session.adapter.inbound.rest.dto;

import com.codeclash.authservice.session.model.Credentials;
import com.codeclash.authservice.session.model.request.CreateSessionRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login credentials: email or username, plus password")
public record CreateSessionRequestBody(
    @Schema(
            description = "Email of the account; send either this or username",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "ada@example.com")
        String email,
    @Schema(
            description = "Username of the account; send either this or email",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            example = "ada_lovelace")
        String username,
    @Schema(
            description = "Account password",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            format = "password",
            example = "Str0ng!pass")
        String password) {

  public CreateSessionRequest toRequest() {
    return new CreateSessionRequest(Credentials.of(this.email, this.username, this.password));
  }
}

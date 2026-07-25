package com.codeclash.contracts.auth.session.remote;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/sessions")
interface SessionClient {

  String VERSION = "1";

  @GetExchange(value = "/{id}", version = SessionClient.VERSION)
  SessionResponse findById(
      @PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);
}

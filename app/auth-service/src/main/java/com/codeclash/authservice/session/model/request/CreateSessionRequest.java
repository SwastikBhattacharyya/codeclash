package com.codeclash.authservice.session.model.request;

import com.codeclash.authservice.session.model.Credentials;

public record CreateSessionRequest(Credentials credentials) {}

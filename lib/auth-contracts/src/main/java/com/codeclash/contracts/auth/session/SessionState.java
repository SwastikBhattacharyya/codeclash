package com.codeclash.contracts.auth.session;

public sealed interface SessionState
    permits SessionState.Anonymous, SessionState.Authenticated, SessionState.Unavailable {

  record Anonymous() implements SessionState {}

  record Authenticated(SessionPrincipal principal) implements SessionState {}

  record Unavailable() implements SessionState {}
}

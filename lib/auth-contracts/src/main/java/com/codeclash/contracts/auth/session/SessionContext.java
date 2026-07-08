package com.codeclash.contracts.auth.session;

final class SessionContext {

  private static final ThreadLocal<SessionState> STATE = new ThreadLocal<>();

  private SessionContext() {}

  static SessionState get() {
    SessionState state = SessionContext.STATE.get();
    return state == null ? new SessionState.Anonymous() : state;
  }

  static void set(SessionState state) {
    SessionContext.STATE.set(state);
  }

  static void clear() {
    SessionContext.STATE.remove();
  }
}

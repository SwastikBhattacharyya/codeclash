package com.codeclash.contracts.auth.session;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(AuthenticatedAspect.ORDER)
@RequiredArgsConstructor
public class AuthenticatedAspect {

  public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

  private final CurrentSession currentSession;

  @Before("@annotation(com.codeclash.contracts.auth.session.Authenticated)")
  public void requireAuthenticated() {
    this.currentSession.requireAuthenticated();
  }
}

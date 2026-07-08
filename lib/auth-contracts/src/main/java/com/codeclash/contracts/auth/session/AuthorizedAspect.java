package com.codeclash.contracts.auth.session;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

@Aspect
@Order(AuthorizedAspect.ORDER)
@RequiredArgsConstructor
public class AuthorizedAspect {

  public static final int ORDER = AuthenticatedAspect.ORDER + 1;

  private final CurrentSession currentSession;

  @Before("@annotation(authorized)")
  public void requireAuthorized(Authorized authorized) {
    this.currentSession.requireAllPermissions(authorized.value());
  }
}

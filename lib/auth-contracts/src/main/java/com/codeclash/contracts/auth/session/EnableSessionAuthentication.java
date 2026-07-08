package com.codeclash.contracts.auth.session;

import com.codeclash.contracts.auth.session.rest.AuthExceptionHandler;
import com.codeclash.contracts.auth.session.rest.AuthOpenApiConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
  BearerSessionFilter.class,
  CurrentSession.class,
  AuthenticatedAspect.class,
  AuthorizedAspect.class,
  AuthExceptionHandler.class,
  AuthOpenApiConfig.class
})
public @interface EnableSessionAuthentication {}

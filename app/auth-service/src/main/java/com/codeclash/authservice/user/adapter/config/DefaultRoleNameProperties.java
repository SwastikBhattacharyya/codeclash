package com.codeclash.authservice.user.adapter.config;

import com.codeclash.authservice.user.port.outbound.DefaultRoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultRoleNameProperties implements DefaultRoleName {

  private final UserProperties userProperties;

  @Override
  public String value() {
    return this.userProperties.defaultRole();
  }
}

package com.codeclash.authservice.user.adapter.inbound.startup;

import com.codeclash.authservice.user.port.inbound.LoadDefaultRoleUseCase;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class UserDefaultRoleRunner implements ApplicationRunner {

  private final LoadDefaultRoleUseCase loadDefaultRoleUseCase;

  @Override
  public void run(@NonNull ApplicationArguments args) {
    this.loadDefaultRoleUseCase.load();
  }
}

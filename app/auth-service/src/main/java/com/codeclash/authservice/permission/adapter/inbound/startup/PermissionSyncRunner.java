package com.codeclash.authservice.permission.adapter.inbound.startup;

import com.codeclash.authservice.permission.port.inbound.SyncPermissionsUseCase;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class PermissionSyncRunner implements ApplicationRunner {

  private final SyncPermissionsUseCase syncPermissionsUseCase;

  @Override
  public void run(@NonNull ApplicationArguments args) {
    this.syncPermissionsUseCase.sync();
  }
}

package com.codeclash.authservice.role.adapter.inbound.startup;

import com.codeclash.authservice.role.adapter.config.DefaultRoleProperties;
import com.codeclash.authservice.role.adapter.config.RoleProperties;
import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.permission.Named;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.request.SyncDefaultRoleRequest;
import com.codeclash.authservice.role.port.inbound.SyncDefaultRolesUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class RoleSyncRunner implements ApplicationRunner {

  private final RoleProperties roleProperties;
  private final SyncDefaultRolesUseCase syncDefaultRolesUseCase;

  @Override
  public void run(@NonNull ApplicationArguments args) {
    List<SyncDefaultRoleRequest<Named>> requests =
        this.roleProperties.defaults().stream().map(RoleSyncRunner::toRequest).toList();
    this.syncDefaultRolesUseCase.sync(requests);
  }

  private static SyncDefaultRoleRequest<Named> toRequest(DefaultRoleProperties role) {
    List<com.codeclash.common.permission.Name> permissions =
        role.permissions().stream().map(com.codeclash.common.permission.Name::of).toList();
    return new SyncDefaultRoleRequest<>(
        Name.of(role.name()), Description.of(role.description()), Permissions.named(permissions));
  }
}

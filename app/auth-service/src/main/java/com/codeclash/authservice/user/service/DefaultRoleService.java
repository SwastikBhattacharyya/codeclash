package com.codeclash.authservice.user.service;

import com.codeclash.authservice.role.api.RoleView;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.exception.InvalidDefaultRoleException;
import com.codeclash.authservice.user.port.inbound.GetDefaultRoleUseCase;
import com.codeclash.authservice.user.port.inbound.LoadDefaultRoleUseCase;
import com.codeclash.authservice.user.port.outbound.DefaultRoleName;
import com.codeclash.authservice.user.port.outbound.RoleLookup;
import io.micrometer.observation.annotation.Observed;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class DefaultRoleService implements LoadDefaultRoleUseCase, GetDefaultRoleUseCase {

  private final RoleLookup roleLookup;
  private final DefaultRoleName defaultRoleName;
  private final AtomicReference<RoleId> defaultRole = new AtomicReference<>();

  @Override
  public void load() {
    RoleId role = this.resolveDefault();
    this.defaultRole.set(role);
    DefaultRoleService.log
        .atInfo()
        .setMessage("default role loaded")
        .addKeyValue("role.id", role.value())
        .log();
  }

  @Override
  public RoleId get() {
    return this.defaultRole.updateAndGet(
        current -> current != null ? current : this.resolveDefault());
  }

  private RoleId resolveDefault() {
    String name = this.defaultRoleName.value();
    RoleView view = this.roleLookup.findByName(name);
    if (!view.isDefault()) throw new InvalidDefaultRoleException(name);
    return RoleId.unsafe(view.id());
  }
}

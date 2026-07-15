package com.codeclash.authservice.user.adapter.outbound.role;

import com.codeclash.authservice.role.api.RoleAccessView;
import com.codeclash.authservice.role.api.RoleView;
import com.codeclash.authservice.role.api.usecase.FindRoleAccessApiUseCase;
import com.codeclash.authservice.role.api.usecase.FindRoleByNameApiUseCase;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.port.outbound.RoleLookup;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleApiAdapter implements RoleLookup {

  private final FindRoleByNameApiUseCase findRoleByNameApiUseCase;
  private final FindRoleAccessApiUseCase findRoleAccessApiUseCase;

  @Override
  public RoleView findByName(String name) {
    return this.findRoleByNameApiUseCase.findByName(name);
  }

  @Override
  public List<String> findPermissions(RoleId role) {
    if (role == null) return List.of();
    return this.findRoleAccessApiUseCase.findByIds(List.of(role.value())).stream()
        .flatMap(access -> access.permissions().stream())
        .toList();
  }
}

package com.codeclash.authservice.session.adapter.outbound.role;

import com.codeclash.authservice.role.api.RoleAccessView;
import com.codeclash.authservice.role.api.usecase.FindRoleAccessApiUseCase;
import com.codeclash.authservice.session.port.outbound.RoleAccessLookup;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleAccessApiAdapter implements RoleAccessLookup {

  private final FindRoleAccessApiUseCase findRoleAccessApiUseCase;

  @Override
  public List<RoleAccessView> findByIds(List<UUID> ids) {
    return this.findRoleAccessApiUseCase.findByIds(ids);
  }
}

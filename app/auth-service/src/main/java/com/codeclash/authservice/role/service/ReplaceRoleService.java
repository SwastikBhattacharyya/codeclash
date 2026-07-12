package com.codeclash.authservice.role.service;

import com.codeclash.authservice.role.model.exception.DefaultRoleModificationException;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.ReplaceRoleRequest;
import com.codeclash.authservice.role.model.result.ReplaceRoleResult;
import com.codeclash.authservice.role.port.inbound.ReplaceRoleUseCase;
import com.codeclash.authservice.role.port.outbound.RolePersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class ReplaceRoleService implements ReplaceRoleUseCase {

  private final RolePersistence rolePersistence;

  @Override
  @Transactional
  public ReplaceRoleResult<Unresolved> replace(ReplaceRoleRequest<Unresolved> request) {
    ReplaceRoleResult<Unresolved> result =
        this.rolePersistence
            .replace(request)
            .orElseThrow(() -> new DefaultRoleModificationException(request.id().value()));
    ReplaceRoleService.log
        .atInfo()
        .setMessage("role replaced")
        .addKeyValue("role.id", result.role().id().value())
        .addKeyValue("created", result.created())
        .log();
    return result;
  }
}

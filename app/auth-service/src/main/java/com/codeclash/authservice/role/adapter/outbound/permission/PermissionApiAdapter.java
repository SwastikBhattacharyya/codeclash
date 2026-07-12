package com.codeclash.authservice.role.adapter.outbound.permission;

import com.codeclash.authservice.permission.api.PermissionView;
import com.codeclash.authservice.permission.api.usecase.FindPermissionsByIdsApiUseCase;
import com.codeclash.authservice.permission.api.usecase.FindPermissionsByNamesApiUseCase;
import com.codeclash.authservice.role.model.permission.PermissionId;
import com.codeclash.authservice.role.port.outbound.PermissionLookup;
import com.codeclash.common.permission.Name;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionApiAdapter implements PermissionLookup {

  private final FindPermissionsByIdsApiUseCase findPermissionsByIdsApiUseCase;
  private final FindPermissionsByNamesApiUseCase findPermissionsByNamesApiUseCase;

  @Override
  public List<PermissionView> findByIds(List<PermissionId> ids) {
    return this.findPermissionsByIdsApiUseCase.findByIds(
        ids.stream().map(PermissionId::value).toList());
  }

  @Override
  public List<PermissionView> findByNames(List<Name> names) {
    return this.findPermissionsByNamesApiUseCase.findByNames(
        names.stream().map(Name::value).toList());
  }
}

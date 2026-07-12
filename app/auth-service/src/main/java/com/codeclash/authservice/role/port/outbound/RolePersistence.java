package com.codeclash.authservice.role.port.outbound;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.CreateRoleRequest;
import com.codeclash.authservice.role.model.request.PatchRoleRequest;
import com.codeclash.authservice.role.model.request.QueryRoleRequest;
import com.codeclash.authservice.role.model.request.ReplaceRoleRequest;
import com.codeclash.authservice.role.model.request.SyncDefaultRoleRequest;
import com.codeclash.authservice.role.model.result.ReplaceRoleResult;
import com.codeclash.common.page.PagedEntity;
import java.util.List;
import java.util.Optional;

public interface RolePersistence {

  PagedEntity<Role<Unresolved>> query(QueryRoleRequest request);

  Optional<Role<Unresolved>> findById(Id id);

  Optional<Role<Unresolved>> findByName(Name name);

  List<Role<Unresolved>> findAllByIdIn(List<Id> ids);

  Role<Unresolved> create(CreateRoleRequest<Unresolved> request);

  Optional<ReplaceRoleResult<Unresolved>> replace(ReplaceRoleRequest<Unresolved> request);

  Optional<Role<Unresolved>> patch(PatchRoleRequest<Unresolved> request);

  boolean deleteById(Id id);

  void lock();

  void demoteDefaultsByNameNotIn(List<Name> names);

  void upsertDefault(SyncDefaultRoleRequest<Unresolved> request);
}

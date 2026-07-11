package com.codeclash.authservice.permission.port.outbound;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.authservice.permission.model.request.QueryPermissionRequest;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.common.permission.Name;
import com.codeclash.common.permission.PermissionDefinition;
import java.util.List;
import java.util.Optional;

public interface PermissionPersistence {

  PagedEntity<Permission> query(QueryPermissionRequest request);

  Optional<Permission> findById(Id id);

  List<Permission> findAllByIdIn(List<Id> ids);

  List<Permission> findAllByNameIn(List<Name> names);

  void lock();

  void deleteByNameNotIn(List<Name> names);

  void upsert(PermissionDefinition definition);
}

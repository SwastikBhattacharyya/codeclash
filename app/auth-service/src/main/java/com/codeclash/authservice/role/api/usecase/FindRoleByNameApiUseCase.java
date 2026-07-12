package com.codeclash.authservice.role.api.usecase;

import com.codeclash.authservice.role.api.RoleView;

public interface FindRoleByNameApiUseCase {

  RoleView findByName(String name);
}

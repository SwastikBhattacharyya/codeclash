package com.codeclash.authservice.permission.api.usecase;

import com.codeclash.authservice.permission.api.PermissionView;
import java.util.List;

public interface FindPermissionsByNamesApiUseCase {

  List<PermissionView> findByNames(List<String> names);
}

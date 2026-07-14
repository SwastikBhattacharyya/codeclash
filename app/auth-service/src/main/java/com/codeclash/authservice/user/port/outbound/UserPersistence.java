package com.codeclash.authservice.user.port.outbound;

import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.password.Hashed;
import com.codeclash.authservice.user.model.request.CreateUserRequest;
import com.codeclash.authservice.user.model.request.PatchUserRequest;
import com.codeclash.authservice.user.model.request.QueryUserRequest;
import com.codeclash.authservice.user.model.request.ReplaceUserRequest;
import com.codeclash.authservice.user.model.result.ReplaceUserResult;
import com.codeclash.common.page.PagedEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserPersistence {

  PagedEntity<User> query(QueryUserRequest request);

  Optional<User> findById(Id id);

  Optional<User> findByEmail(Email email);

  Optional<User> findByUsername(Username username);

  Map<Id, RoleId> findRoleIds(List<Id> ids);

  User create(CreateUserRequest<Hashed> request);

  ReplaceUserResult replace(ReplaceUserRequest<Hashed> request);

  Optional<User> patch(PatchUserRequest<Hashed> request);

  Optional<User> deleteById(Id id);

  List<User> assignRoleWhereMissing(RoleId role);
}

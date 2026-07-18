package com.codeclash.authservice.session.service;

import com.codeclash.authservice.role.api.RoleAccessView;
import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.outbound.RoleAccessLookup;
import com.codeclash.authservice.session.port.outbound.UserAuthenticator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SessionAuthorizer {

  private final UserAuthenticator userAuthenticator;
  private final RoleAccessLookup roleAccessLookup;

  List<AuthorizedSession> authorize(List<Session> sessions) {
    List<UUID> userIds =
        sessions.stream().map(session -> session.userId().value()).distinct().toList();
    Map<UUID, UUID> roleIds = this.userAuthenticator.findRoleIds(userIds);
    Map<UUID, RoleAccessView> access =
        this.roleAccessLookup.findByIds(roleIds.values().stream().distinct().toList()).stream()
            .collect(Collectors.toMap(RoleAccessView::id, Function.identity()));
    return sessions.stream()
        .map(
            session -> {
              UUID roleId = roleIds.get(session.userId().value());
              RoleAccessView view = roleId == null ? null : access.get(roleId);
              return view == null
                  ? new AuthorizedSession(session, null, List.of())
                  : new AuthorizedSession(session, view.name(), view.permissions());
            })
        .toList();
  }
}

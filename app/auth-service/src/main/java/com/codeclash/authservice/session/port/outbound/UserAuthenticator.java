package com.codeclash.authservice.session.port.outbound;

import com.codeclash.authservice.session.model.Credentials;
import com.codeclash.authservice.user.api.UserView;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserAuthenticator {

  Optional<UserView> authenticate(Credentials credentials);

  boolean isVerified(UUID userId);

  Map<UUID, UUID> findRoleIds(List<UUID> userIds);
}

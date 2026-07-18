package com.codeclash.authservice.session.model.result;

import com.codeclash.authservice.session.model.Session;
import java.util.List;

public record AuthorizedSession(Session session, String roleName, List<String> permissions) {}

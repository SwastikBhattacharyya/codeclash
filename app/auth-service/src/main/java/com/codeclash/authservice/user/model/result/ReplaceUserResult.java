package com.codeclash.authservice.user.model.result;

import com.codeclash.authservice.user.model.User;

public record ReplaceUserResult(User user, boolean created) {}

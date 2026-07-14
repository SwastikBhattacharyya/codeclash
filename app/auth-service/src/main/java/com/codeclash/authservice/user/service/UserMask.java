package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;

record UserMask(boolean email, boolean role) {

  User apply(User user) {
    User masked = user;
    if (!this.email) masked = masked.withEmail(null);
    if (!this.role) masked = masked.withRole(null);
    return masked;
  }
}

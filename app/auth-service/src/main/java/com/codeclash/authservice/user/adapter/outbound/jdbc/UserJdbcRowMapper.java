package com.codeclash.authservice.user.adapter.outbound.jdbc;

import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.Name;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class UserJdbcRowMapper implements RowMapper<User> {

  @Override
  public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
    UUID verificationToken = rs.getObject("verification_token", UUID.class);
    UUID roleId = rs.getObject("role_id", UUID.class);
    return new User(
        Id.unsafe(rs.getObject("id", UUID.class)),
        Name.unsafe(rs.getString("name")),
        Username.unsafe(rs.getString("username")),
        Email.unsafe(rs.getString("email")),
        Password.unsafeHashed(rs.getString("password_hash")),
        new Verified(rs.getBoolean("is_verified")),
        verificationToken == null ? null : VerificationToken.unsafe(verificationToken),
        roleId == null ? null : RoleId.unsafe(roleId),
        new CreatedAt(rs.getTimestamp("created_at").toInstant()),
        new UpdatedAt(rs.getTimestamp("updated_at").toInstant()));
  }
}

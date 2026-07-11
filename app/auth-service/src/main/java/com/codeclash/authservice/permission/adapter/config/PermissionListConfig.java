package com.codeclash.authservice.permission.adapter.config;

import com.codeclash.common.permission.PermissionList;
import com.codeclash.contracts.auth.AuthPermissions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermissionListConfig {

  @Bean
  PermissionList authPermissions() {
    return new AuthPermissions();
  }
}

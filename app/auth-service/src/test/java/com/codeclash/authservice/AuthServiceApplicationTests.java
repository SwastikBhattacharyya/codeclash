package com.codeclash.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class AuthServiceApplicationTests {

  @Test
  void contextLoads() {}

  @Test
  void verifyApplicationModules() {
    ApplicationModules.of(AuthServiceApplication.class).verify();
  }
}

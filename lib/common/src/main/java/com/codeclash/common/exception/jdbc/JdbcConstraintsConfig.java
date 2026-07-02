package com.codeclash.common.exception.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class JdbcConstraintsConfig {

  @Bean
  ConstraintFinder constraintFinder(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      String product = connection.getMetaData().getDatabaseProductName();
      return ConstraintFinders.forDatabase(DatabaseDriver.fromProductName(product));
    }
  }
}

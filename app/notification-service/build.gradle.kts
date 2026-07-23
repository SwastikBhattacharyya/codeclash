buildscript {
  dependencies {
    classpath(platform(libs.spring.boot.bom))

    classpath(libs.flyway.database.postgresql)
    classpath(libs.postgresql)
  }
}

plugins {
  java
  alias(libs.plugins.spring.boot)

  alias(libs.plugins.flyway)
  alias(libs.plugins.freefair.lombok)
}

group = "com.codeclash"
version = "0.1.0"
description = "notification-service"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

flyway {
  url = "jdbc:postgresql://localhost:${providers.environmentVariable("DB_PORT").getOrElse("5432")}/notification"
  user = providers.environmentVariable("DB_USER").getOrElse("codeclash")
  password = providers.environmentVariable("DB_PASSWORD").getOrElse("codeclash")
  locations = arrayOf("filesystem:src/main/resources/db/migration")
  cleanDisabled = false
}

dependencies {
  implementation(platform(libs.spring.boot.bom))

  implementation(platform(libs.datasource.micrometer.bom))

  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.aspectj)
  implementation(libs.spring.boot.starter.jackson)
  implementation(libs.spring.boot.starter.jdbc)
  implementation(libs.spring.boot.starter.kafka)
  implementation(libs.spring.boot.starter.mail)
  implementation(libs.spring.boot.starter.opentelemetry)
  implementation(libs.spring.boot.starter.thymeleaf)
  implementation(libs.spring.boot.starter.validation)

  implementation(libs.datasource.micrometer.opentelemetry)
  implementation(libs.datasource.micrometer.spring.boot)
  implementation(libs.opentelemetry.logback.appender)

  implementation(project(":lib:auth-contracts"))
  implementation(project(":lib:common"))

  developmentOnly(platform(libs.spring.boot.bom))
  developmentOnly(libs.spring.boot.devtools)

  runtimeOnly(libs.micrometer.registry.otlp)
  runtimeOnly(libs.postgresql)

  annotationProcessor(platform(libs.spring.boot.bom))
  annotationProcessor(libs.spring.boot.configuration.processor)
}

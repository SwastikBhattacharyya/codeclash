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
version = "0.2.0"
description = "auth-service"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

flyway {
  url = "jdbc:postgresql://localhost:${providers.environmentVariable("DB_PORT").getOrElse("5432")}/auth"
  user = providers.environmentVariable("DB_USER").getOrElse("codeclash")
  password = providers.environmentVariable("DB_PASSWORD").getOrElse("codeclash")
  locations = arrayOf("filesystem:src/main/resources/db/migration")
  cleanDisabled = false
}

springBoot {
  buildInfo {
    excludes = setOf("time")
  }
}

dependencies {
  implementation(platform(libs.spring.boot.bom))
  implementation(platform(libs.spring.modulith.bom))

  implementation(platform(libs.datasource.micrometer.bom))
  implementation(platform(libs.namastack.outbox.bom))

  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.aspectj)
  implementation(libs.spring.boot.starter.jdbc)
  implementation(libs.spring.boot.starter.kafka)
  implementation(libs.spring.boot.starter.opentelemetry)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.webmvc)
  implementation(libs.spring.modulith.starter.core)
  implementation(libs.spring.security.crypto)

  implementation(libs.datasource.micrometer.opentelemetry)
  implementation(libs.datasource.micrometer.spring.boot)
  implementation(libs.namastack.outbox.kafka)
  implementation(libs.namastack.outbox.starter.jdbc)
  implementation(libs.opentelemetry.logback.appender)
  implementation(libs.springdoc.openapi.starter.webmvc.ui)

  implementation(project(":lib:auth-contracts"))
  implementation(project(":lib:common"))
  implementation(project(":lib:contest-contracts"))
  implementation(project(":lib:problem-contracts"))

  developmentOnly(platform(libs.spring.boot.bom))
  developmentOnly(libs.spring.boot.devtools)

  runtimeOnly(libs.bouncycastle.bcprov)
  runtimeOnly(libs.micrometer.registry.otlp)
  runtimeOnly(libs.namastack.outbox.observability)
  runtimeOnly(libs.postgresql)

  annotationProcessor(platform(libs.spring.boot.bom))
  annotationProcessor(libs.spring.boot.configuration.processor)

  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.modulith.starter.test)

  testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test> {
  useJUnitPlatform()
}

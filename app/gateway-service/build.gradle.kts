plugins {
  java
  alias(libs.plugins.spring.boot)

  alias(libs.plugins.freefair.lombok)
}

group = "com.codeclash"
version = "0.3.0"
description = "gateway-service"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

springBoot {
  buildInfo {
    excludes = setOf("time")
  }
}

dependencies {
  implementation(platform(libs.spring.boot.bom))
  implementation(platform(libs.spring.cloud.bom))

  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.opentelemetry)
  implementation(libs.spring.boot.starter.restclient)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.webmvc)
  implementation(libs.spring.cloud.starter.gateway.server.webmvc)

  implementation(libs.opentelemetry.logback.appender)
  implementation(libs.springdoc.openapi.starter.webmvc.ui)

  implementation(project(":lib:auth-contracts"))
  implementation(project(":lib:common"))

  developmentOnly(platform(libs.spring.boot.bom))
  developmentOnly(libs.spring.boot.devtools)

  runtimeOnly(libs.micrometer.registry.otlp)

  annotationProcessor(platform(libs.spring.boot.bom))
  annotationProcessor(libs.spring.boot.configuration.processor)
}

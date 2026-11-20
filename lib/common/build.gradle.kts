plugins {
  `java-library`

  alias(libs.plugins.freefair.lombok)
}

group = "com.codeclash"
version = "1.0.0"
description = "common"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(platform(libs.spring.boot.bom))

  implementation(libs.spring.boot)

  api(libs.jackson.annotations)
  api(libs.jackson.databind.nullable)

  compileOnly(platform(libs.namastack.outbox.bom))

  compileOnly(libs.micrometer.observation)
  compileOnly(libs.namastack.outbox.api)
  compileOnly(libs.namastack.outbox.kafka)
  compileOnly(libs.spring.boot.starter.jdbc)
  compileOnly(libs.spring.boot.starter.kafka)
  compileOnly(libs.spring.boot.starter.validation)
  compileOnly(libs.spring.boot.starter.webmvc)
  compileOnly(libs.springdoc.openapi.starter.webmvc.ui)

  compileOnly(libs.postgresql)
}

tasks.withType<JavaCompile>().configureEach {
  options.compilerArgs.add("-parameters")
}

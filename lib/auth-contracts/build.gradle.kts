plugins {
  `java-library`

  alias(libs.plugins.freefair.lombok)
}

group = "com.codeclash"
version = "0.2.0"
description = "auth-contracts"

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

  api(project(":lib:common"))

  compileOnly(libs.spring.boot.starter.aspectj)
  compileOnly(libs.spring.boot.starter.validation)
  compileOnly(libs.spring.boot.starter.webmvc)
  compileOnly(libs.springdoc.openapi.starter.webmvc.ui)
}

tasks.withType<JavaCompile>().configureEach {
  options.compilerArgs.add("-parameters")
}

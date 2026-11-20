plugins {
  `java-library`
}

group = "com.codeclash"
version = "1.0.0"
description = "runner-contracts"

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

  api(project(":lib:problem-contracts"))
}

tasks.withType<JavaCompile>().configureEach {
  options.compilerArgs.add("-parameters")
}

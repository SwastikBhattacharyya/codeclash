package com.codeclash.runnerservice.sandbox.adapter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SandboxProperties.class)
class SandboxConfig {}

package com.codeclash.notificationservice.user.adapter.config;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration(proxyBeanMethods = false)
class MailTemplateConfig {

  private static final String PREFIX = "templates/";
  private static final String TEXT_PATTERN = "*.txt";
  private static final int ORDER = 0;

  @Bean
  ITemplateResolver textTemplateResolver() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix(MailTemplateConfig.PREFIX);
    resolver.setTemplateMode(TemplateMode.TEXT);
    resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resolver.setResolvablePatterns(Set.of(MailTemplateConfig.TEXT_PATTERN));
    resolver.setOrder(MailTemplateConfig.ORDER);
    resolver.setCheckExistence(true);
    return resolver;
  }
}

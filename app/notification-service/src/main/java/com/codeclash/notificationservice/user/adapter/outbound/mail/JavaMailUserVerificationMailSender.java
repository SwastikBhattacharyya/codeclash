package com.codeclash.notificationservice.user.adapter.outbound.mail;

import com.codeclash.notificationservice.user.adapter.config.UserVerificationMailProperties;
import com.codeclash.notificationservice.user.model.User;
import com.codeclash.notificationservice.user.port.outbound.UserVerificationMailSender;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
public class JavaMailUserVerificationMailSender implements UserVerificationMailSender {

  private static final String HTML_TEMPLATE = "mail/user-verification";
  private static final String TEXT_TEMPLATE = "mail/user-verification.txt";
  private static final String NAME_VARIABLE = "name";
  private static final String TOKEN_VARIABLE = "token";

  private final JavaMailSender javaMailSender;
  private final ITemplateEngine templateEngine;
  private final UserVerificationMailProperties userVerificationMailProperties;

  @Override
  public void send(User user) {
    Context context = new Context();
    context.setVariable(JavaMailUserVerificationMailSender.NAME_VARIABLE, user.name().value());
    context.setVariable(
        JavaMailUserVerificationMailSender.TOKEN_VARIABLE, user.verificationToken().value());
    String html =
        this.templateEngine.process(JavaMailUserVerificationMailSender.HTML_TEMPLATE, context);
    String text =
        this.templateEngine.process(JavaMailUserVerificationMailSender.TEXT_TEMPLATE, context);
    this.javaMailSender.send(
        mimeMessage -> {
          MimeMessageHelper helper =
              new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
          helper.setFrom(this.userVerificationMailProperties.from());
          helper.setTo(user.email().value());
          helper.setSubject(this.userVerificationMailProperties.subject());
          helper.setText(text, html);
        });
  }
}

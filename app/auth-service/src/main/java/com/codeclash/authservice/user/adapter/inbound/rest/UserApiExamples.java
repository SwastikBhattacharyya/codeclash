package com.codeclash.authservice.user.adapter.inbound.rest;

import com.codeclash.authservice.user.adapter.inbound.rest.dto.EmailProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.LengthProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.MaxLengthProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.MinLettersProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.RequiredVersionProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.RoleIdProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.UserIdProperties;
import com.codeclash.authservice.user.adapter.inbound.rest.dto.UsernameProperties;
import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.Name;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.exception.DuplicateEmailException;
import com.codeclash.authservice.user.model.exception.DuplicateUsernameException;
import com.codeclash.authservice.user.model.exception.InvalidUsernameException;
import com.codeclash.authservice.user.model.exception.NullUsernameException;
import com.codeclash.authservice.user.model.exception.UsernameTooLongException;
import com.codeclash.authservice.user.model.exception.UsernameTooShortException;
import com.codeclash.authservice.user.model.exception.EmailTooLongException;
import com.codeclash.authservice.user.model.exception.InvalidEmailException;
import com.codeclash.authservice.user.model.exception.InvalidNameException;
import com.codeclash.authservice.user.model.exception.InvalidRoleIdException;
import com.codeclash.authservice.user.model.exception.InvalidUserIdException;
import com.codeclash.authservice.user.model.exception.InvalidVerificationTokenException;
import com.codeclash.authservice.user.model.exception.NameTooLongException;
import com.codeclash.authservice.user.model.exception.NameTooShortException;
import com.codeclash.authservice.user.model.exception.NullEmailException;
import com.codeclash.authservice.user.model.exception.NullNameException;
import com.codeclash.authservice.user.model.exception.NullPasswordException;
import com.codeclash.authservice.user.model.exception.NullRoleException;
import com.codeclash.authservice.user.model.exception.NullUserIdException;
import com.codeclash.authservice.user.model.exception.NullVerifiedException;
import com.codeclash.authservice.user.model.exception.PasswordMissingDigitException;
import com.codeclash.authservice.user.model.exception.PasswordMissingLowercaseException;
import com.codeclash.authservice.user.model.exception.PasswordMissingSpecialCharacterException;
import com.codeclash.authservice.user.model.exception.PasswordMissingUppercaseException;
import com.codeclash.authservice.user.model.exception.PasswordTooLongException;
import com.codeclash.authservice.user.model.exception.PasswordTooShortException;
import com.codeclash.authservice.user.model.exception.RoleNotFoundException;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.model.exception.VerificationTokenMismatchException;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.common.openapi.ApiExamples;
import com.codeclash.common.openapi.ApiSchemas;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.rest.dto.RequiredPermissionsProperties;
import io.swagger.v3.oas.models.examples.Example;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserApiExamples {

  public static final String USER_NOT_FOUND = "user.USER_NOT_FOUND";
  public static final String DUPLICATE_EMAIL = "user.DUPLICATE_EMAIL";
  public static final String DUPLICATE_USERNAME = "user.DUPLICATE_USERNAME";
  public static final String ROLE_NOT_FOUND = "user.ROLE_NOT_FOUND";
  public static final String NULL_NAME = "user.NULL_NAME";
  public static final String NAME_TOO_SHORT = "user.NAME_TOO_SHORT";
  public static final String NAME_TOO_LONG = "user.NAME_TOO_LONG";
  public static final String INVALID_NAME = "user.INVALID_NAME";
  public static final String NULL_USERNAME = "user.NULL_USERNAME";
  public static final String USERNAME_TOO_SHORT = "user.USERNAME_TOO_SHORT";
  public static final String USERNAME_TOO_LONG = "user.USERNAME_TOO_LONG";
  public static final String INVALID_USERNAME = "user.INVALID_USERNAME";
  public static final String NULL_EMAIL = "user.NULL_EMAIL";
  public static final String EMAIL_TOO_LONG = "user.EMAIL_TOO_LONG";
  public static final String INVALID_EMAIL = "user.INVALID_EMAIL";
  public static final String NULL_PASSWORD = "user.NULL_PASSWORD";
  public static final String PASSWORD_TOO_SHORT = "user.PASSWORD_TOO_SHORT";
  public static final String PASSWORD_TOO_LONG = "user.PASSWORD_TOO_LONG";
  public static final String PASSWORD_MISSING_LOWERCASE = "user.PASSWORD_MISSING_LOWERCASE";
  public static final String PASSWORD_MISSING_UPPERCASE = "user.PASSWORD_MISSING_UPPERCASE";
  public static final String PASSWORD_MISSING_DIGIT = "user.PASSWORD_MISSING_DIGIT";
  public static final String PASSWORD_MISSING_SPECIAL_CHARACTER =
      "user.PASSWORD_MISSING_SPECIAL_CHARACTER";
  public static final String NULL_ROLE = "user.NULL_ROLE";
  public static final String INVALID_ROLE_ID = "user.INVALID_ROLE_ID";
  public static final String INVALID_USER_ID = "user.INVALID_USER_ID";
  public static final String NULL_USER_ID = "user.NULL_USER_ID";
  public static final String NULL_VERIFIED = "user.NULL_VERIFIED";
  public static final String INVALID_VERIFICATION_TOKEN = "user.INVALID_VERIFICATION_TOKEN";
  public static final String VERIFICATION_TOKEN_MISMATCH = "user.VERIFICATION_TOKEN_MISMATCH";
  public static final String FORBIDDEN_READ_FIELDS = "user.FORBIDDEN_READ_FIELDS";
  public static final String FORBIDDEN_WRITE = "user.FORBIDDEN_WRITE";
  public static final String FORBIDDEN_WRITE_OWN = "user.FORBIDDEN_WRITE_OWN";
  public static final String FORBIDDEN_FIELDS = "user.FORBIDDEN_FIELDS";
  public static final String FORBIDDEN_FIELDS_OWN = "user.FORBIDDEN_FIELDS_OWN";
  public static final String FORBIDDEN_ROLE_PERMISSIONS = "user.FORBIDDEN_ROLE_PERMISSIONS";
  public static final String UNKNOWN_SORT_FIELD = "user.UNKNOWN_SORT_FIELD";

  private static final UUID SAMPLE_ID = UUID.fromString("01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f");
  private static final UUID SAMPLE_WRONG_VERSION_ID =
      UUID.fromString("3f2b8c1e-4d5a-4b6c-8e7f-9a0b1c2d3e4f");
  private static final String SAMPLE_SORT_FIELD = "rating";
  private static final String SAMPLE_EMAIL = "ada@example.com";
  private static final String SAMPLE_USERNAME = "ada_lovelace";
  private static final List<String> SORT_FIELDS =
      List.of("created_at", "email", "id", "name", "updated_at", "username", "verified");

  private static Example forbidden(String... permissions) {
    List<String> missing = List.of(permissions);
    return ApiExamples.error(
        new ForbiddenException(missing), new RequiredPermissionsProperties(missing));
  }

  @Bean
  OpenApiCustomizer userOpenApiExamples(SpringDocConfigProperties springDocProperties) {
    return openApi -> {
      ApiSchemas.register(
          springDocProperties,
          openApi.getComponents(),
          UserIdProperties.class,
          EmailProperties.class,
          UsernameProperties.class,
          RoleIdProperties.class,
          LengthProperties.class,
          MaxLengthProperties.class,
          MinLettersProperties.class,
          RequiredVersionProperties.class);
      openApi
          .getComponents()
          .addExamples(
              UserApiExamples.USER_NOT_FOUND,
              ApiExamples.error(
                  new UserNotFoundException(UserApiExamples.SAMPLE_ID),
                  new UserIdProperties(UserApiExamples.SAMPLE_ID)))
          .addExamples(
              UserApiExamples.DUPLICATE_EMAIL,
              ApiExamples.error(
                  new DuplicateEmailException(UserApiExamples.SAMPLE_EMAIL),
                  new EmailProperties(UserApiExamples.SAMPLE_EMAIL)))
          .addExamples(
              UserApiExamples.DUPLICATE_USERNAME,
              ApiExamples.error(
                  new DuplicateUsernameException(UserApiExamples.SAMPLE_USERNAME),
                  new UsernameProperties(UserApiExamples.SAMPLE_USERNAME)))
          .addExamples(
              UserApiExamples.ROLE_NOT_FOUND,
              ApiExamples.error(
                  new RoleNotFoundException(UserApiExamples.SAMPLE_ID),
                  new RoleIdProperties(UserApiExamples.SAMPLE_ID)))
          .addExamples(UserApiExamples.NULL_NAME, ApiExamples.error(new NullNameException()))
          .addExamples(
              UserApiExamples.NAME_TOO_SHORT,
              ApiExamples.error(
                  new NameTooShortException("A"),
                  new LengthProperties(Name.MIN_SIZE, Name.MAX_SIZE)))
          .addExamples(
              UserApiExamples.NAME_TOO_LONG,
              ApiExamples.error(
                  new NameTooLongException("a".repeat(Name.MAX_SIZE + 1)),
                  new LengthProperties(Name.MIN_SIZE, Name.MAX_SIZE)))
          .addExamples(
              UserApiExamples.INVALID_NAME,
              ApiExamples.error(
                  new InvalidNameException("42"),
                  new MinLettersProperties(Name.MIN_LETTERS)))
          .addExamples(
              UserApiExamples.NULL_USERNAME, ApiExamples.error(new NullUsernameException()))
          .addExamples(
              UserApiExamples.USERNAME_TOO_SHORT,
              ApiExamples.error(
                  new UsernameTooShortException("ad"),
                  new LengthProperties(Username.MIN_SIZE, Username.MAX_SIZE)))
          .addExamples(
              UserApiExamples.USERNAME_TOO_LONG,
              ApiExamples.error(
                  new UsernameTooLongException("a".repeat(Username.MAX_SIZE + 1)),
                  new LengthProperties(Username.MIN_SIZE, Username.MAX_SIZE)))
          .addExamples(
              UserApiExamples.INVALID_USERNAME,
              ApiExamples.error(new InvalidUsernameException("ada lovelace")))
          .addExamples(UserApiExamples.NULL_EMAIL, ApiExamples.error(new NullEmailException()))
          .addExamples(
              UserApiExamples.EMAIL_TOO_LONG,
              ApiExamples.error(
                  new EmailTooLongException("a".repeat(Email.MAX_SIZE + 1)),
                  new MaxLengthProperties(Email.MAX_SIZE)))
          .addExamples(
              UserApiExamples.INVALID_EMAIL,
              ApiExamples.error(new InvalidEmailException("ada.example.com")))
          .addExamples(
              UserApiExamples.NULL_PASSWORD,
              ApiExamples.error(new NullPasswordException()))
          .addExamples(
              UserApiExamples.PASSWORD_TOO_SHORT,
              ApiExamples.error(
                  new PasswordTooShortException(),
                  new LengthProperties(Password.MIN_SIZE, Password.MAX_SIZE)))
          .addExamples(
              UserApiExamples.PASSWORD_TOO_LONG,
              ApiExamples.error(
                  new PasswordTooLongException(),
                  new LengthProperties(Password.MIN_SIZE, Password.MAX_SIZE)))
          .addExamples(
              UserApiExamples.PASSWORD_MISSING_LOWERCASE,
              ApiExamples.error(new PasswordMissingLowercaseException()))
          .addExamples(
              UserApiExamples.PASSWORD_MISSING_UPPERCASE,
              ApiExamples.error(new PasswordMissingUppercaseException()))
          .addExamples(
              UserApiExamples.PASSWORD_MISSING_DIGIT,
              ApiExamples.error(new PasswordMissingDigitException()))
          .addExamples(
              UserApiExamples.PASSWORD_MISSING_SPECIAL_CHARACTER,
              ApiExamples.error(new PasswordMissingSpecialCharacterException()))
          .addExamples(UserApiExamples.NULL_ROLE, ApiExamples.error(new NullRoleException()))
          .addExamples(
              UserApiExamples.INVALID_ROLE_ID,
              ApiExamples.error(
                  new InvalidRoleIdException(UserApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(RoleId.VERSION)))
          .addExamples(
              UserApiExamples.INVALID_USER_ID,
              ApiExamples.error(
                  new InvalidUserIdException(UserApiExamples.SAMPLE_WRONG_VERSION_ID),
                  new RequiredVersionProperties(Id.VERSION)))
          .addExamples(UserApiExamples.NULL_USER_ID, ApiExamples.error(new NullUserIdException()))
          .addExamples(
              UserApiExamples.NULL_VERIFIED,
              ApiExamples.error(new NullVerifiedException()))
          .addExamples(
              UserApiExamples.INVALID_VERIFICATION_TOKEN,
              ApiExamples.error(
                  new InvalidVerificationTokenException(UserApiExamples.SAMPLE_ID),
                  new RequiredVersionProperties(VerificationToken.VERSION)))
          .addExamples(
              UserApiExamples.VERIFICATION_TOKEN_MISMATCH,
              ApiExamples.error(new VerificationTokenMismatchException()))
          .addExamples(
              UserApiExamples.FORBIDDEN_READ_FIELDS,
              UserApiExamples.forbidden(
                  AuthPermissions.USER_READ_EMAIL, AuthPermissions.USER_READ_ROLE))
          .addExamples(
              UserApiExamples.FORBIDDEN_WRITE,
              UserApiExamples.forbidden(AuthPermissions.USER_WRITE))
          .addExamples(
              UserApiExamples.FORBIDDEN_WRITE_OWN,
              UserApiExamples.forbidden(AuthPermissions.USER_WRITE_OWN))
          .addExamples(
              UserApiExamples.FORBIDDEN_FIELDS,
              UserApiExamples.forbidden(
                  AuthPermissions.USER_WRITE_IS_VERIFIED,
                  AuthPermissions.USER_WRITE_ROLE,
                  AuthPermissions.USER_WRITE_USERNAME))
          .addExamples(
              UserApiExamples.FORBIDDEN_FIELDS_OWN,
              UserApiExamples.forbidden(
                  AuthPermissions.USER_WRITE_IS_VERIFIED_OWN,
                  AuthPermissions.USER_WRITE_ROLE_OWN,
                  AuthPermissions.USER_WRITE_USERNAME_OWN))
          .addExamples(
              UserApiExamples.FORBIDDEN_ROLE_PERMISSIONS,
              UserApiExamples.forbidden(AuthPermissions.ROLE_WRITE, AuthPermissions.USER_WRITE))
          .addExamples(
              UserApiExamples.UNKNOWN_SORT_FIELD,
              ApiExamples.error(
                  new UnknownSortFieldException(
                      UserApiExamples.SAMPLE_SORT_FIELD, UserApiExamples.SORT_FIELDS),
                  new UnknownSortFieldProperties(
                      UserApiExamples.SAMPLE_SORT_FIELD, UserApiExamples.SORT_FIELDS)));
    };
  }
}

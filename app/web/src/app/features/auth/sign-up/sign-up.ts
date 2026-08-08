import { Component, Injector, inject, signal } from '@angular/core';
import { type FieldTree, FormField, FormRoot, form } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { AccountApi } from '../../../core/account/account-api';
import { SIGN_IN_PATH, VERIFY_EMAIL_PATH } from '../../../core/auth/auth-paths';
import {
  emailRules,
  nameRules,
  PASSWORD_RULES,
  passwordRules,
  usernameRules,
} from '../../../core/auth/user-rules';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  type SubmitFailure,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Panel } from '../../../ui/panel';
import { PasswordField } from '../../../ui/password-field';
import { TextField } from '../../../ui/text-field';

type SignUpModel = {
  name: string;
  username: string;
  email: string;
  password: string;
};

@Component({
  selector: 'cc-sign-up',
  imports: [FormRoot, FormField, RouterLink, Alert, Button, Panel, PasswordField, TextField],
  templateUrl: './sign-up.html',
})
export class SignUp {
  private readonly api = inject(AccountApi);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly model = signal<SignUpModel>({ name: '', username: '', email: '', password: '' });

  protected readonly signInPath = SIGN_IN_PATH;
  protected readonly verifyEmailPath = VERIFY_EMAIL_PATH;
  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly formError = signal<string | null>(null);
  protected readonly signUpForm = form(
    this.model,
    (path) => {
      nameRules(path.name);
      usernameRules(path.username);
      emailRules(path.email);
      passwordRules(path.password);
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          const value = field().value();
          const username = value.username.trim();
          try {
            await this.api.signUp({
              name: value.name.trim(),
              username,
              email: value.email.trim(),
              password: value.password,
            });
          } catch (error) {
            return this.fail(
              toSubmitFailure(error, {
                name: field.name,
                username: field.username,
                email: field.email,
                password: field.password,
              }),
              [field.name, field.username, field.email, field.password],
            );
          }
          await this.router.navigate([VERIFY_EMAIL_PATH], { queryParams: { username, new: 1 } });
          return undefined;
        },
        onInvalid: (field) =>
          focusFirstInvalid([field.name, field.username, field.email, field.password]),
      },
    },
  );

  private fail(
    failure: SubmitFailure,
    fields: readonly FieldTree<unknown>[],
  ): SubmitFailure['fieldErrors'] {
    this.formError.set(failure.formError);
    if (failure.fieldErrors.length > 0) {
      focusFirstInvalidAfterRender(this.injector, fields);
    }
    return failure.fieldErrors;
  }
}

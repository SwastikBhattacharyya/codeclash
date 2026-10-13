import { Component, Injector, inject, signal } from '@angular/core';
import {
  type FieldTree,
  FormField,
  FormRoot,
  form,
  required,
  validate,
} from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { AccountApi } from '../../../core/account/account-api';
import { PROFILE_PATH } from '../../../core/auth/auth-paths';
import { PASSWORD_RULES, passwordRules } from '../../../core/auth/user-rules';
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
import { ToastStore } from '../../../ui/toast-store';

type ChangePasswordModel = {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
};

@Component({
  selector: 'cc-change-password',
  imports: [FormRoot, FormField, RouterLink, Alert, Button, Panel, PasswordField],
  templateUrl: './change-password.html',
})
export class ChangePassword {
  private readonly api = inject(AccountApi);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly toasts = inject(ToastStore);
  private readonly model = signal<ChangePasswordModel>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  protected readonly profilePath = PROFILE_PATH;
  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly formError = signal<string | null>(null);
  protected readonly passwordForm = form(
    this.model,
    (path) => {
      required(path.currentPassword, { message: 'Enter your current password' });
      passwordRules(path.newPassword);
      required(path.confirmPassword, { message: 'Repeat the new password' });
      validate(path.confirmPassword, (context) =>
        context.value() !== '' && context.value() !== context.valueOf(path.newPassword)
          ? { kind: 'mismatch', message: "This doesn't match the new password" }
          : null,
      );
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          const { currentPassword, newPassword } = field().value();
          let otherSessionsSignedOut: boolean;
          try {
            ({ otherSessionsSignedOut } = await this.api.changePassword(
              currentPassword,
              newPassword,
            ));
          } catch (error) {
            return this.fail(
              toSubmitFailure(error, {
                currentPassword: field.currentPassword,
                password: field.newPassword,
              }),
              [field.currentPassword, field.newPassword, field.confirmPassword],
            );
          }
          if (otherSessionsSignedOut) {
            this.toasts.show('Password changed. Your other devices were signed out.');
          } else {
            this.toasts.show(
              'Password changed, but some other devices may still be signed in. Change it again later to sign them out.',
              'info',
            );
          }
          await this.router.navigateByUrl(PROFILE_PATH);
          return undefined;
        },
        onInvalid: (field) =>
          focusFirstInvalid([field.currentPassword, field.newPassword, field.confirmPassword]),
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

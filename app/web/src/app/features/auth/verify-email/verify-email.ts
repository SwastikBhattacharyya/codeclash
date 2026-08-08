import { Component, Injector, inject, signal } from '@angular/core';
import {
  type FieldTree,
  FormField,
  FormRoot,
  form,
  pattern,
  required,
} from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AccountApi } from '../../../core/account/account-api';
import { PROFILE_PATH, SIGN_IN_PATH } from '../../../core/auth/auth-paths';
import { AuthStore } from '../../../core/auth/auth-store';
import { requiredText } from '../../../core/auth/user-rules';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  type SubmitFailure,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Panel } from '../../../ui/panel';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';

type VerifyModel = {
  username: string;
  token: string;
};

const TOKEN_PATTERN =
  /^\s*[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\s*$/;

@Component({
  selector: 'cc-verify-email',
  imports: [FormRoot, FormField, RouterLink, Alert, Button, Panel, TextField],
  templateUrl: './verify-email.html',
})
export class VerifyEmail {
  private readonly api = inject(AccountApi);
  private readonly auth = inject(AuthStore);
  private readonly toasts = inject(ToastStore);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly query = inject(ActivatedRoute).snapshot.queryParamMap;
  private readonly model = signal<VerifyModel>({
    username: this.query.get('username') ?? '',
    token: '',
  });

  protected readonly signInPath = SIGN_IN_PATH;
  protected readonly justSignedUp = this.query.get('new') === '1';
  protected readonly hint = this.query.get('hint');
  protected readonly formError = signal<string | null>(null);
  protected readonly verifyForm = form(
    this.model,
    (path) => {
      requiredText(path.username, 'Enter your username');
      required(path.token, { message: 'Enter the code from the email' });
      pattern(path.token, TOKEN_PATTERN, {
        message: 'Paste the code exactly as it appears in the email',
      });
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          const username = field().value().username.trim();
          try {
            await this.api.verifyEmail(username, field().value().token.trim());
          } catch (error) {
            return this.fail(toSubmitFailure(error, { username: field.username }), [
              field.username,
              field.token,
            ]);
          }
          if (this.auth.isAuthenticated()) {
            this.toasts.show('Your email is verified');
            await this.router.navigateByUrl(PROFILE_PATH);
          } else {
            await this.router.navigate([SIGN_IN_PATH], {
              queryParams: { login: username, verified: 1 },
            });
          }
          return undefined;
        },
        onInvalid: (field) => focusFirstInvalid([field.username, field.token]),
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

import { Component, inject, signal } from '@angular/core';
import { FormField, FormRoot, form, required } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toApiError } from '../../../core/api/api-errors';
import { SIGN_UP_PATH, VERIFY_EMAIL_PATH } from '../../../core/auth/auth-paths';
import { AuthStore } from '../../../core/auth/auth-store';
import { safeRedirect } from '../../../core/auth/safe-redirect';
import { requiredText } from '../../../core/auth/user-rules';
import { focusFirstInvalid, toSubmitFailure } from '../../../core/forms/form-errors';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Checkbox } from '../../../ui/checkbox';
import { Panel } from '../../../ui/panel';
import { PasswordField } from '../../../ui/password-field';
import { TextField } from '../../../ui/text-field';

type SignInModel = {
  login: string;
  password: string;
  rememberMe: boolean;
};

const INVALID_CREDENTIALS_MESSAGE = 'Email/username or password is incorrect';
const UNVERIFIED_USER = 'UNVERIFIED_USER';

@Component({
  selector: 'cc-sign-in',
  imports: [
    FormRoot,
    FormField,
    RouterLink,
    Alert,
    Button,
    Checkbox,
    Panel,
    PasswordField,
    TextField,
  ],
  templateUrl: './sign-in.html',
})
export class SignIn {
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly query = inject(ActivatedRoute).snapshot.queryParamMap;
  private readonly model = signal<SignInModel>({
    login: this.query.get('login') ?? '',
    password: '',
    rememberMe: false,
  });

  protected readonly signUpPath = SIGN_UP_PATH;
  protected readonly verifyEmailPath = VERIFY_EMAIL_PATH;
  protected readonly justVerified = this.query.get('verified') === '1';
  protected readonly formError = signal<string | null>(null);
  protected readonly signInForm = form(
    this.model,
    (path) => {
      requiredText(path.login, 'Enter your email or username');
      required(path.password, { message: 'Enter your password' });
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          const { login, password, rememberMe } = field().value();
          const trimmedLogin = login.trim();
          try {
            await this.auth.signIn(trimmedLogin, password, rememberMe);
          } catch (error) {
            if (toApiError(error)?.code === UNVERIFIED_USER) {
              await this.router.navigate([VERIFY_EMAIL_PATH], {
                queryParams: trimmedLogin.includes('@')
                  ? { hint: 'username' }
                  : { username: trimmedLogin, hint: 'unverified' },
              });
              return undefined;
            }
            this.formError.set(
              toSubmitFailure(error, {}, { INVALID_CREDENTIALS: INVALID_CREDENTIALS_MESSAGE })
                .formError,
            );
            return undefined;
          }
          await this.router.navigateByUrl(safeRedirect(this.query.get('redirect')));
          return undefined;
        },
        onInvalid: (field) => focusFirstInvalid([field.login, field.password]),
      },
    },
  );
}

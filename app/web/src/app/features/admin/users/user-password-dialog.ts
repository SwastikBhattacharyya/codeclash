import { Component, computed, Injector, inject, input, output, signal } from '@angular/core';
import { FormField, FormRoot, form } from '@angular/forms/signals';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import { PASSWORD_RULES, passwordRules } from '../../../core/auth/user-rules';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { PasswordField } from '../../../ui/password-field';
import { ForbiddenAlert } from '../forbidden-alert';
import { type AdminUser, USER_FORBIDDEN_MESSAGE, UsersApi } from './users-api';

type PasswordModel = {
  password: string;
};

@Component({
  selector: 'cc-user-password-dialog',
  imports: [FormRoot, FormField, Button, Dialog, PasswordField, ForbiddenAlert],
  template: `
    <cc-dialog [title]="title()" (closed)="close()">
      <form [formRoot]="passwordForm" class="flex flex-col gap-5">
        @if (formError(); as message) {
          <cc-forbidden-alert [message]="message" [permissions]="missing()" />
        }
        <cc-password-field
          [formField]="passwordForm.password"
          label="New password"
          autocomplete="new-password"
          [rules]="passwordRules"
          hint="Their existing sessions stay signed in."
        />
        <div class="flex flex-wrap justify-end gap-3">
          <button
            ccButton
            variant="ghost"
            type="button"
            [disabled]="passwordForm().submitting()"
            (click)="close()"
          >
            Cancel
          </button>
          <button ccButton type="submit" [loading]="passwordForm().submitting()">
            Set password
          </button>
        </div>
      </form>
    </cc-dialog>
  `,
})
export class UserPasswordDialog {
  private readonly api = inject(UsersApi);
  private readonly injector = inject(Injector);

  readonly user = input.required<AdminUser>();
  readonly saved = output<AdminUser>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly title = computed(() => `Set password for @${this.user().username}`);
  protected readonly formError = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);

  private readonly model = signal<PasswordModel>({ password: '' });
  protected readonly passwordForm = form(
    this.model,
    (path) => {
      passwordRules(path.password);
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          this.missing.set([]);
          try {
            const user = await this.api.update(this.user().id, {
              password: field().value().password,
            });
            this.saved.emit(user);
            return undefined;
          } catch (error) {
            if (isGone(error)) {
              this.gone.emit();
              return undefined;
            }
            const failure = toSubmitFailure(
              error,
              { password: field.password },
              { FORBIDDEN: USER_FORBIDDEN_MESSAGE },
            );
            this.formError.set(failure.formError);
            this.missing.set(missingPermissions(error));
            if (failure.fieldErrors.length > 0) {
              focusFirstInvalidAfterRender(this.injector, [field.password]);
            }
            return failure.fieldErrors;
          }
        },
        onInvalid: (field) => focusFirstInvalid([field.password]),
      },
    },
  );

  protected close(): void {
    if (!this.passwordForm().submitting()) {
      this.closed.emit();
    }
  }
}

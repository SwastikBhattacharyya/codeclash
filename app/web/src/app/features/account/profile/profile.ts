import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import {
  afterNextRender,
  Component,
  computed,
  type ElementRef,
  Injector,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { type FieldTree, FormField, FormRoot, form } from '@angular/forms/signals';
import { RouterLink } from '@angular/router';
import { LucideBadgeCheck, LucideCircleExclamationPoint } from '@lucide/angular';
import { AccountApi } from '../../../core/account/account-api';
import { type ProfileChanges, type User, userUrl } from '../../../core/account/user';
import { errorMessage } from '../../../core/api/api-errors';
import { CHANGE_PASSWORD_PATH, VERIFY_EMAIL_PATH } from '../../../core/auth/auth-paths';
import { AuthStore } from '../../../core/auth/auth-store';
import { nameRules, usernameRules } from '../../../core/auth/user-rules';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  type SubmitFailure,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Panel } from '../../../ui/panel';
import { Spinner } from '../../../ui/spinner';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';

type ProfileModel = {
  name: string;
  username: string;
};

const FORBIDDEN_MESSAGE = "You don't have permission to change this.";

@Component({
  selector: 'cc-profile',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    RouterLink,
    LucideBadgeCheck,
    LucideCircleExclamationPoint,
    Alert,
    Button,
    Panel,
    Spinner,
    TextField,
  ],
  templateUrl: './profile.html',
})
export class Profile {
  private readonly api = inject(AccountApi);
  private readonly injector = inject(Injector);
  private readonly auth = inject(AuthStore);
  private readonly toasts = inject(ToastStore);
  private readonly model = signal<ProfileModel>({ name: '', username: '' });

  protected readonly changePasswordPath = CHANGE_PASSWORD_PATH;
  protected readonly verifyEmailPath = VERIFY_EMAIL_PATH;
  protected readonly roleName = computed(() => this.auth.session()?.roleName ?? null);
  protected readonly user = httpResource<User>(() => {
    const userId = this.auth.session()?.userId;
    return userId ? userUrl(userId) : undefined;
  });
  protected readonly loadError = computed(() =>
    this.user.error() ? errorMessage(this.user.error()) : null,
  );
  protected readonly editing = signal(false);
  private readonly editButton = viewChild<ElementRef<HTMLButtonElement>>('editButton');
  protected readonly formError = signal<string | null>(null);
  protected readonly profileForm = form(
    this.model,
    (path) => {
      nameRules(path.name);
      usernameRules(path.username);
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          const current = this.user.value();
          if (!current) {
            return undefined;
          }
          const value = field().value();
          const changes: ProfileChanges = {};
          const name = value.name.trim();
          const username = value.username.trim();
          if (name !== current.name) {
            changes.name = name;
          }
          if (username !== current.username) {
            changes.username = username;
          }
          if (Object.keys(changes).length > 0) {
            try {
              this.user.set(await this.api.updateProfile(current.id, changes));
            } catch (error) {
              return this.fail(
                toSubmitFailure(
                  error,
                  { name: field.name, username: field.username },
                  { FORBIDDEN: FORBIDDEN_MESSAGE },
                ),
                [field.name, field.username],
              );
            }
          }
          this.stopEditing();
          if (Object.keys(changes).length > 0) {
            this.toasts.show('Profile updated');
          }
          return undefined;
        },
        onInvalid: (field) => focusFirstInvalid([field.name, field.username]),
      },
    },
  );

  protected startEditing(): void {
    const current = this.user.value();
    if (!current) {
      return;
    }
    this.model.set({ name: current.name, username: current.username });
    this.profileForm().reset();
    this.editing.set(true);
    afterNextRender(() => this.profileForm.name().focusBoundControl(), {
      injector: this.injector,
    });
  }

  protected cancelEditing(): void {
    this.stopEditing();
  }

  private stopEditing(): void {
    this.editing.set(false);
    afterNextRender(() => this.editButton()?.nativeElement.focus(), { injector: this.injector });
  }

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

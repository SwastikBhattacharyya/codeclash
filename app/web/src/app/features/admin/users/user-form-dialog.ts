import {
  Component,
  computed,
  Injector,
  inject,
  input,
  linkedSignal,
  output,
  signal,
} from '@angular/core';
import {
  disabled,
  type FieldTree,
  FormField,
  FormRoot,
  form,
  hidden,
} from '@angular/forms/signals';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
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
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Button } from '../../../ui/button';
import { Checkbox } from '../../../ui/checkbox';
import { Dialog } from '../../../ui/dialog';
import { PasswordField } from '../../../ui/password-field';
import { Select, type SelectOption } from '../../../ui/select';
import { TextField } from '../../../ui/text-field';
import { ForbiddenAlert } from '../forbidden-alert';
import { UserAccess } from './user-access';
import {
  type AdminUser,
  type Role,
  USER_FORBIDDEN_MESSAGE,
  type UserChanges,
  type UserCreate,
  UsersApi,
} from './users-api';

type UserModel = {
  name: string;
  username: string;
  email: string;
  password: string;
  roleId: string;
  verified: boolean;
};

export type UserSaved = {
  user: AdminUser;
  created: boolean;
};

const DEFAULT_ROLE_LABEL = 'Default role';
const UNKNOWN_ROLE_LABEL = 'Unknown role';
const NO_ROLE_LABEL = 'No role';

@Component({
  selector: 'cc-user-form-dialog',
  imports: [
    FormRoot,
    FormField,
    Button,
    Checkbox,
    Dialog,
    PasswordField,
    Select,
    TextField,
    ForbiddenAlert,
  ],
  templateUrl: './user-form-dialog.html',
})
export class UserFormDialog {
  private readonly api = inject(UsersApi);
  private readonly injector = inject(Injector);
  private readonly access = inject(UserAccess);

  readonly user = input<AdminUser | null>(null);
  readonly roles = input<readonly Role[]>([]);
  readonly saved = output<UserSaved>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly passwordRules = PASSWORD_RULES;
  protected readonly creating = computed(() => this.user() === null);
  protected readonly title = computed(() => {
    const user = this.user();
    return user ? `Edit @${user.username}` : 'New user';
  });
  protected readonly roleOptions = computed<SelectOption[]>(() => {
    const options = this.roles().map((role) => ({ value: role.id, label: role.name }));
    const user = this.user();
    if (user === null) {
      return [{ value: '', label: DEFAULT_ROLE_LABEL }, ...options];
    }
    if (user.roleId === null) {
      return [{ value: '', label: NO_ROLE_LABEL }, ...options];
    }
    if (!options.some((option) => option.value === user.roleId)) {
      return [{ value: user.roleId, label: UNKNOWN_ROLE_LABEL }, ...options];
    }
    return options;
  });
  protected readonly roleHint = computed(() => {
    const user = this.user();
    if (this.access.isSelf(user)) {
      return "You can't change your own role here";
    }
    return this.access.canWriteRole(user) ? '' : "You don't have permission to change roles";
  });
  protected readonly formError = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);

  private readonly model = linkedSignal<UserModel>(() => {
    const user = this.user();
    return {
      name: user?.name ?? '',
      username: user?.username ?? '',
      email: user?.email ?? '',
      password: '',
      roleId: user?.roleId ?? '',
      verified: user?.isVerified ?? false,
    };
  });
  protected readonly userForm = form(
    this.model,
    (path) => {
      nameRules(path.name);
      usernameRules(path.username);
      emailRules(path.email);
      passwordRules(path.password);
      disabled(path.username, {
        when: () => !this.creating() && !this.access.canWriteUsername(this.user()),
      });
      hidden(path.email, { when: () => !this.creating() && !this.access.canReadEmail() });
      hidden(path.password, { when: () => !this.creating() });
      hidden(path.roleId, { when: () => !this.access.canReadRoles() });
      disabled(path.roleId, {
        when: () => this.access.isSelf(this.user()) || !this.access.canWriteRole(this.user()),
      });
      hidden(path.verified, { when: () => !this.access.canWriteVerified(this.user()) });
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          this.missing.set([]);
          const current = this.user();
          try {
            if (current === null) {
              this.saved.emit({
                user: await this.api.create(this.createBody(field)),
                created: true,
              });
              return undefined;
            }
            const changes = this.changesFrom(current, field);
            if (Object.keys(changes).length === 0) {
              this.closed.emit();
              return undefined;
            }
            this.saved.emit({ user: await this.api.update(current.id, changes), created: false });
            return undefined;
          } catch (error) {
            if (current && isGone(error)) {
              this.gone.emit();
              return undefined;
            }
            const fields = this.fieldList(field);
            const failure = toSubmitFailure(
              error,
              {
                name: field.name,
                username: field.username,
                email: field.email,
                password: field.password,
                roleId: field.roleId,
              },
              { FORBIDDEN: USER_FORBIDDEN_MESSAGE },
            );
            this.formError.set(failure.formError);
            this.missing.set(missingPermissions(error));
            if (failure.fieldErrors.length > 0) {
              focusFirstInvalidAfterRender(this.injector, fields);
            }
            return failure.fieldErrors;
          }
        },
        onInvalid: (field) => focusFirstInvalid(this.fieldList(field)),
      },
    },
  );

  protected close(): void {
    if (!this.userForm().submitting()) {
      this.closed.emit();
    }
  }

  private createBody(field: FieldTree<UserModel>): UserCreate {
    const value = field().value();
    const body: UserCreate = {
      name: value.name.trim(),
      username: value.username.trim(),
      email: value.email.trim(),
      password: value.password,
    };
    if (UserFormDialog.isEditable(field.roleId) && value.roleId !== '') {
      body.roleId = value.roleId;
    }
    if (UserFormDialog.isEditable(field.verified) && value.verified) {
      body.verified = true;
    }
    return body;
  }

  private changesFrom(user: AdminUser, field: FieldTree<UserModel>): UserChanges {
    const value = field().value();
    const changes: UserChanges = {};
    const name = value.name.trim();
    const username = value.username.trim();
    const email = value.email.trim();
    if (name !== user.name) {
      changes.name = name;
    }
    if (UserFormDialog.isEditable(field.username) && username !== user.username) {
      changes.username = username;
    }
    if (UserFormDialog.isEditable(field.email) && email !== user.email) {
      changes.email = email;
    }
    if (
      UserFormDialog.isEditable(field.roleId) &&
      value.roleId !== '' &&
      value.roleId !== user.roleId
    ) {
      changes.roleId = value.roleId;
    }
    if (UserFormDialog.isEditable(field.verified) && value.verified !== user.isVerified) {
      changes.verified = value.verified;
    }
    return changes;
  }

  private fieldList(field: FieldTree<UserModel>): FieldTree<unknown>[] {
    return [field.name, field.username, field.email, field.password, field.roleId];
  }

  private static isEditable(field: FieldTree<unknown>): boolean {
    return !field().hidden() && !field().disabled();
  }
}

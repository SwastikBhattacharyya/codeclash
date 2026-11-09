import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import {
  Component,
  computed,
  DOCUMENT,
  effect,
  Injector,
  inject,
  linkedSignal,
  signal,
  untracked,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormField, FormRoot, form } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucideCopy, LucideLock, LucideTrash } from '@lucide/angular';
import { adminSection } from '../../../core/admin/admin-sections';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import type { Page } from '../../../core/api/page';
import { AuthStore } from '../../../core/auth/auth-store';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Spinner } from '../../../ui/spinner';
import { TextArea } from '../../../ui/text-area';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { ForbiddenAlert } from '../forbidden-alert';
import { PermissionPicker } from './permission-picker';
import { RoleAccess } from './role-access';
import { RoleDeleteDialog } from './role-delete-dialog';
import {
  ROLE_DESCRIPTION_MAX_SIZE,
  ROLE_NAME_HINT,
  roleDescriptionRules,
  roleNameRules,
} from './role-rules';
import {
  type AdminRole,
  type Permission,
  permissionsRequest,
  ROLE_FORBIDDEN_MESSAGE,
  type RoleChanges,
  type RoleInput,
  RolesApi,
  roleUrl,
} from './roles-api';

type RoleModel = {
  name: string;
  description: string;
};

type RoleDraft = RoleModel & {
  permissions: readonly string[];
};

const EMPTY_DRAFT: RoleDraft = { name: '', description: '', permissions: [] };
const COPY_SUFFIX = '_copy';
const PERMISSIONS_NOT_FOUND = 'PERMISSIONS_NOT_FOUND';
const DISCARD_MESSAGE = 'Discard unsaved changes?';
const SUBMIT_OVERRIDES: Readonly<Record<string, string>> = {
  FORBIDDEN: ROLE_FORBIDDEN_MESSAGE,
  DEFAULT_ROLE_MODIFICATION: "Default roles can't be changed.",
  [PERMISSIONS_NOT_FOUND]: 'Some permissions no longer exist. The list has been refreshed.',
};

@Component({
  selector: 'cc-role-detail',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    RouterLink,
    LucideArrowLeft,
    LucideCopy,
    LucideLock,
    LucideTrash,
    Alert,
    Button,
    ForbiddenAlert,
    PermissionPicker,
    RoleDeleteDialog,
    Spinner,
    TextArea,
    TextField,
  ],
  host: {
    class: 'flex flex-col gap-6 sm:gap-8',
    '(window:beforeunload)': 'onBeforeUnload($event)',
  },
  templateUrl: './role-detail.html',
})
export class RoleDetail {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly document = inject(DOCUMENT);
  private readonly injector = inject(Injector);
  private readonly toasts = inject(ToastStore);
  private readonly auth = inject(AuthStore);
  private readonly api = inject(RolesApi);
  private readonly params = toSignal(this.route.paramMap, { requireSync: true });
  private readonly queryParams = toSignal(this.route.queryParamMap, { requireSync: true });
  private leaving = false;

  protected readonly access = inject(RoleAccess);
  protected readonly section = adminSection('roles');
  protected readonly nameHint = ROLE_NAME_HINT;
  protected readonly descriptionMaxSize = ROLE_DESCRIPTION_MAX_SIZE;

  protected readonly roleId = computed(() => this.params().get('id'));
  protected readonly creating = computed(() => this.roleId() === null);
  private readonly sourceId = computed(() =>
    this.creating() ? this.queryParams().get('from') : null,
  );

  protected readonly role = httpResource<AdminRole>(() => {
    const id = this.roleId() ?? this.sourceId();
    return id ? roleUrl(id) : undefined;
  });
  protected readonly loadedRole = computed(() =>
    !this.creating() && this.role.hasValue() ? this.role.value() : null,
  );
  protected readonly permissions = httpResource<Page<Permission>>(() =>
    this.access.canReadPermissions() ? permissionsRequest() : undefined,
  );
  protected readonly permissionList = computed<readonly Permission[]>(() =>
    this.permissions.hasValue() ? this.permissions.value().data : [],
  );

  protected readonly ready = computed(() => {
    if (!this.creating()) {
      return this.loadedRole() !== null;
    }
    return this.sourceId() === null || !this.role.isLoading();
  });
  protected readonly sourceError = computed(() =>
    this.creating() && this.sourceId() !== null && this.role.error()
      ? "The role to duplicate couldn't be loaded, so this form starts empty."
      : null,
  );
  protected readonly editable = computed(() => {
    if (this.creating()) {
      return this.access.canWrite();
    }
    const role = this.loadedRole();
    return role !== null && this.access.canEdit(role);
  });
  protected readonly own = computed(() => {
    const role = this.loadedRole();
    return role !== null && this.access.isOwn(role);
  });
  protected readonly title = computed(() =>
    this.creating() ? 'New role' : (this.loadedRole()?.name ?? 'Role'),
  );

  private readonly baseline = computed<RoleDraft>(() => {
    if (!this.role.hasValue()) {
      return EMPTY_DRAFT;
    }
    const role = this.role.value();
    return {
      name: this.creating() ? `${role.name}${COPY_SUFFIX}` : role.name,
      description: role.description,
      permissions: role.permissions,
    };
  });
  private readonly model = linkedSignal<RoleModel>(() => ({
    name: this.baseline().name,
    description: this.baseline().description,
  }));
  protected readonly selected = linkedSignal<readonly string[]>(() => this.baseline().permissions);
  private readonly knownSelected = computed<readonly string[]>(() => {
    if (!this.permissions.hasValue()) {
      return this.selected();
    }
    const known = new Set(this.permissionList().map((permission) => permission.id));
    return this.selected().filter((id) => known.has(id));
  });
  protected readonly dirty = computed(() => {
    const baseline = this.baseline();
    const model = this.model();
    return (
      model.name.trim() !== baseline.name ||
      model.description.trim() !== baseline.description ||
      !RoleDetail.sameIds(this.knownSelected(), baseline.permissions)
    );
  });

  protected readonly formError = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);
  protected readonly deleteOpen = signal(false);

  protected readonly roleForm = form(
    this.model,
    (path) => {
      roleNameRules(path.name);
      roleDescriptionRules(path.description);
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          this.missing.set([]);
          const value = field().value();
          const input: RoleInput = {
            name: value.name.trim(),
            description: value.description.trim(),
            permissions: [...this.knownSelected()],
          };
          const current = this.loadedRole();
          try {
            if (current === null) {
              const created = await this.api.create(input);
              this.toasts.show(`Role “${created.name}” created`);
              await this.leave([this.section.path, created.id]);
              return undefined;
            }
            const changes = this.changesFrom(current, input);
            if (Object.keys(changes).length === 0) {
              return undefined;
            }
            const updated = await this.api.update(current.id, changes);
            const wasOwn = this.access.isOwn(current);
            this.role.set(updated);
            this.toasts.show(`Role “${updated.name}” saved`);
            if (wasOwn) {
              await this.auth.refresh();
            }
            return undefined;
          } catch (error) {
            if (current !== null && isGone(error)) {
              await this.onGone();
              return undefined;
            }
            if (toApiError(error)?.code === PERMISSIONS_NOT_FOUND) {
              this.permissions.reload();
            }
            const failure = toSubmitFailure(
              error,
              { name: field.name, description: field.description },
              SUBMIT_OVERRIDES,
            );
            this.formError.set(failure.formError);
            this.missing.set(missingPermissions(error));
            if (failure.fieldErrors.length > 0) {
              focusFirstInvalidAfterRender(this.injector, [field.name, field.description]);
            }
            return failure.fieldErrors;
          }
        },
        onInvalid: (field) => focusFirstInvalid([field.name, field.description]),
      },
    },
  );

  protected readonly loadError = computed(() => {
    const error = this.role.error();
    return error && !isGone(error) ? errorMessage(error) : null;
  });

  constructor() {
    effect(() => {
      const error = this.role.error();
      if (error && !this.creating() && isGone(error)) {
        untracked(() => void this.onGone());
      }
    });
  }

  canLeave(): boolean {
    if (!this.hasUnsavedChanges()) {
      return true;
    }
    return this.document.defaultView?.confirm(DISCARD_MESSAGE) ?? true;
  }

  protected onBeforeUnload(event: BeforeUnloadEvent): void {
    if (this.hasUnsavedChanges()) {
      event.preventDefault();
    }
  }

  protected duplicateParams(role: AdminRole): Record<string, string> {
    return { from: role.id };
  }

  protected newPath(): string {
    return `${this.section.path}/new`;
  }

  protected discard(): void {
    this.model.set({ name: this.baseline().name, description: this.baseline().description });
    this.selected.set(this.baseline().permissions);
    this.formError.set(null);
    this.missing.set([]);
    this.roleForm().reset();
  }

  protected async onDeleted(role: AdminRole): Promise<void> {
    this.deleteOpen.set(false);
    this.toasts.show(`Role “${role.name}” deleted`);
    await this.leave([this.section.path]);
  }

  protected async onGone(): Promise<void> {
    this.deleteOpen.set(false);
    this.toasts.show('That role no longer exists', 'error');
    await this.leave([this.section.path]);
  }

  private hasUnsavedChanges(): boolean {
    return !this.leaving && this.editable() && this.dirty();
  }

  private async leave(commands: readonly string[]): Promise<void> {
    this.leaving = true;
    try {
      this.leaving = await this.router.navigate(commands);
    } catch {
      this.leaving = false;
    }
  }

  private changesFrom(role: AdminRole, input: RoleInput): RoleChanges {
    const changes: RoleChanges = {};
    if (input.name !== role.name) {
      changes.name = input.name;
    }
    if (input.description !== role.description) {
      changes.description = input.description;
    }
    if (!RoleDetail.sameIds(input.permissions, role.permissions)) {
      changes.permissions = input.permissions;
    }
    return changes;
  }

  private static sameIds(left: readonly string[], right: readonly string[]): boolean {
    if (left.length !== right.length) {
      return false;
    }
    const ids = new Set(right);
    return left.every((id) => ids.has(id));
  }
}

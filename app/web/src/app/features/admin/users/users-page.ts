import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import {
  afterNextRender,
  Component,
  computed,
  type ElementRef,
  Injector,
  inject,
  linkedSignal,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormField, FormRoot, form } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  LucideKeyRound,
  LucideListChecks,
  LucidePencil,
  LucidePlus,
  LucideTrash,
} from '@lucide/angular';
import { debounceTime } from 'rxjs';
import { adminSection } from '../../../core/admin/admin-sections';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { DataTable, type TableColumn, TableRow, type TableSort } from '../../../ui/data-table';
import { FIRST_PAGE, Pager } from '../../../ui/pager';
import { Select, type SelectOption } from '../../../ui/select';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { AdminHeading } from '../admin-heading';
import { UserAccess } from './user-access';
import { UserDeleteDialog } from './user-delete-dialog';
import { UserFormDialog, type UserSaved } from './user-form-dialog';
import { UserPasswordDialog } from './user-password-dialog';
import {
  type AdminUser,
  type Role,
  rolesRequest,
  USER_SEARCH_FIELDS,
  USER_SORT_FIELDS,
  type UserQuery,
  type UserSearchField,
  type UserSortField,
  usersRequest,
} from './users-api';

type SearchModel = {
  text: string;
};

type FormTarget = {
  user: AdminUser | null;
};

const SEARCH_DEBOUNCE_MS = 300;
const DEFAULT_SORT: UserSortField = 'created_at';
const DEFAULT_SEARCH_FIELD: UserSearchField = 'username';
const SEARCH_FIELD_LABELS: Record<UserSearchField, string> = {
  username: 'Username',
  name: 'Name',
  email: 'Email',
};
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const UNKNOWN_ROLE_LABEL = 'Unknown role';
const VERIFIED_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  { value: 'true', label: 'Verified' },
  { value: 'false', label: 'Unverified' },
];

@Component({
  selector: 'cc-users-page',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    RouterLink,
    LucideKeyRound,
    LucideListChecks,
    LucidePencil,
    LucidePlus,
    LucideTrash,
    AdminHeading,
    Alert,
    Button,
    DataTable,
    Pager,
    Select,
    TableRow,
    TextField,
    UserDeleteDialog,
    UserFormDialog,
    UserPasswordDialog,
  ],
  host: { class: 'flex flex-col gap-6 sm:gap-8' },
  templateUrl: './users-page.html',
})
export class UsersPage {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toasts = inject(ToastStore);
  private readonly injector = inject(Injector);
  private readonly newButton = viewChild.required<ElementRef<HTMLButtonElement>>('newButton');
  private readonly params = toSignal(this.route.queryParamMap, { requireSync: true });

  protected readonly access = inject(UserAccess);
  protected readonly section = adminSection('users');
  protected readonly submissionsPath = adminSection('submissions').path;
  protected readonly verifiedOptions = VERIFIED_OPTIONS;

  protected readonly columns = computed<TableColumn[]>(() => [
    { label: 'Name', sortField: 'name', sticky: 'start' },
    { label: 'Username', sortField: 'username' },
    ...(this.access.canReadEmail() ? [{ label: 'Email', sortField: 'email' }] : []),
    { label: 'Verified', sortField: 'verified' },
    ...(this.access.canReadRoles() ? [{ label: 'Role' }] : []),
    { label: 'Joined', sortField: 'created_at' },
    { label: 'Actions', sticky: 'end' },
  ]);

  protected readonly query = computed<UserQuery>(() => {
    const params = this.params();
    const canReadEmail = this.access.canReadEmail();
    const page = Number.parseInt(params.get('page') ?? '', 10);
    const field = USER_SEARCH_FIELDS.find(
      (candidate) => candidate === params.get('in') && (candidate !== 'email' || canReadEmail),
    );
    const sort = USER_SORT_FIELDS.find(
      (candidate) => candidate === params.get('sort') && (candidate !== 'email' || canReadEmail),
    );
    const verified = params.get('verified');
    const roleId = params.get('role');
    return {
      text: params.get('q')?.trim() ?? '',
      field: field ?? DEFAULT_SEARCH_FIELD,
      roleId:
        this.access.canReadRoles() && roleId !== null && UUID_PATTERN.test(roleId) ? roleId : null,
      verified: verified === 'true' ? true : verified === 'false' ? false : null,
      page: Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE,
      sort: sort ?? DEFAULT_SORT,
      direction: params.get('direction') === 'asc' ? 'asc' : 'desc',
    };
  });
  protected readonly sort = computed<TableSort>(() => ({
    field: this.query().sort,
    direction: this.query().direction,
  }));
  protected readonly verifiedFilter = computed(() => {
    const verified = this.query().verified;
    return verified === null ? '' : String(verified);
  });
  protected readonly hasFilters = computed(() => {
    const query = this.query();
    return query.text !== '' || query.roleId !== null || query.verified !== null;
  });

  protected readonly users = httpResource<Page<AdminUser>>(() => usersRequest(this.query()));
  protected readonly shownUsers = lastValue(this.users);
  protected readonly rows = computed(() => this.shownUsers()?.data ?? []);
  protected readonly emptyText = computed(() => {
    if (this.hasFilters()) {
      return 'No users match these filters';
    }
    if (this.query().page > FIRST_PAGE) {
      return 'This page is empty';
    }
    return 'No users yet';
  });

  protected readonly roles = httpResource<Page<Role>>(() =>
    this.access.canReadRoles() ? rolesRequest() : undefined,
  );
  protected readonly roleList = computed<readonly Role[]>(() =>
    this.roles.hasValue() ? this.roles.value().data : [],
  );
  private readonly roleNames = computed(
    () => new Map(this.roleList().map((role) => [role.id, role.name])),
  );
  protected readonly roleFilterOptions = computed<SelectOption[]>(() => {
    const options = this.roleList().map((role) => ({ value: role.id, label: role.name }));
    const roleId = this.query().roleId;
    if (roleId !== null && !options.some((option) => option.value === roleId)) {
      options.unshift({ value: roleId, label: UNKNOWN_ROLE_LABEL });
    }
    return [{ value: '', label: 'All roles' }, ...options];
  });
  protected readonly searchFieldOptions = computed<SelectOption[]>(() =>
    USER_SEARCH_FIELDS.filter((field) => field !== 'email' || this.access.canReadEmail()).map(
      (field) => ({ value: field, label: SEARCH_FIELD_LABELS[field] }),
    ),
  );
  protected readonly searchLabel = computed(
    () => `Search by ${SEARCH_FIELD_LABELS[this.query().field].toLowerCase()}`,
  );

  protected readonly formTarget = signal<FormTarget | null>(null);
  protected readonly passwordTarget = signal<AdminUser | null>(null);
  protected readonly deleteTarget = signal<AdminUser | null>(null);

  private searchedText: string | null = null;
  private readonly textParam = computed(() => this.query().text);
  private readonly searchModel = linkedSignal<string, SearchModel>({
    source: this.textParam,
    computation: (text, previous) => {
      const echoed = text === this.searchedText;
      this.searchedText = null;
      return previous && (echoed || previous.value.text.trim() === text)
        ? previous.value
        : { text };
    },
  });
  protected readonly searchForm = form(this.searchModel, {
    submission: {
      action: async (field) => {
        await this.search(field().value().text);
        return undefined;
      },
    },
  });

  constructor() {
    toObservable(computed(() => this.searchModel().text))
      .pipe(debounceTime(SEARCH_DEBOUNCE_MS), takeUntilDestroyed())
      .subscribe((text) => void this.search(text));
  }

  protected roleName(roleId: string | null): string {
    if (roleId === null || !this.roles.hasValue()) {
      return '—';
    }
    return this.roleNames().get(roleId) ?? UNKNOWN_ROLE_LABEL;
  }

  protected openCreate(): void {
    this.formTarget.set({ user: null });
  }

  protected openEdit(user: AdminUser): void {
    this.formTarget.set({ user });
  }

  protected openPassword(user: AdminUser): void {
    this.passwordTarget.set(user);
  }

  protected openDelete(user: AdminUser): void {
    this.deleteTarget.set(user);
  }

  protected onSearchField(field: string): void {
    void this.navigate({ in: field === DEFAULT_SEARCH_FIELD ? null : field, page: null });
  }

  protected onRoleFilter(roleId: string): void {
    void this.navigate({ role: roleId === '' ? null : roleId, page: null });
  }

  protected onVerifiedFilter(verified: string): void {
    void this.navigate({ verified: verified === '' ? null : verified, page: null });
  }

  protected onSort(sort: TableSort): void {
    void this.navigate({
      sort: sort.field === DEFAULT_SORT ? null : sort.field,
      direction: sort.direction === 'desc' ? null : sort.direction,
      page: null,
    });
  }

  protected onSaved(saved: UserSaved): void {
    this.formTarget.set(null);
    this.toasts.show(`User @${saved.user.username} ${saved.created ? 'created' : 'updated'}`);
    this.users.reload();
  }

  protected onPasswordSaved(user: AdminUser): void {
    this.passwordTarget.set(null);
    this.toasts.show(`Password updated for @${user.username}`);
    this.users.reload();
  }

  protected onDeleted(user: AdminUser): void {
    this.deleteTarget.set(null);
    this.toasts.show(`User @${user.username} deleted`);
    this.refreshAfterRemoval();
  }

  protected onGone(): void {
    this.formTarget.set(null);
    this.passwordTarget.set(null);
    this.deleteTarget.set(null);
    this.toasts.show('That user no longer exists', 'error');
    this.refreshAfterRemoval();
  }

  private refreshAfterRemoval(): void {
    afterNextRender(() => this.newButton().nativeElement.focus(), { injector: this.injector });
    const page = this.shownUsers();
    if (page && page.page > FIRST_PAGE && page.data.length <= 1) {
      const previous = page.page - 1;
      void this.navigate({ page: previous === FIRST_PAGE ? null : String(previous) });
      return;
    }
    this.users.reload();
  }

  private search(text: string): Promise<boolean> {
    const trimmed = text.trim();
    if (trimmed === this.query().text) {
      return Promise.resolve(true);
    }
    this.searchedText = trimmed;
    return this.navigate({ q: trimmed === '' ? null : trimmed, page: null }, true);
  }

  private navigate(
    queryParams: Record<string, string | null>,
    replaceUrl = false,
  ): Promise<boolean> {
    return this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      queryParamsHandling: 'merge',
      replaceUrl,
    });
  }
}

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
  LucideCopy,
  LucideEye,
  LucideLock,
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
import { RoleAccess } from './role-access';
import { RoleDeleteDialog } from './role-delete-dialog';
import {
  type AdminRole,
  ROLE_SORT_FIELDS,
  ROLE_TYPES,
  type RoleQuery,
  type RoleSortField,
  rolesListRequest,
} from './roles-api';

type SearchModel = {
  name: string;
};

const SEARCH_DEBOUNCE_MS = 300;
const DEFAULT_SORT: RoleSortField = 'name';
const TYPE_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  { value: 'default', label: 'Default' },
  { value: 'custom', label: 'Custom' },
];

@Component({
  selector: 'cc-roles-page',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    RouterLink,
    LucideCopy,
    LucideEye,
    LucideLock,
    LucidePencil,
    LucidePlus,
    LucideTrash,
    AdminHeading,
    Alert,
    Button,
    DataTable,
    Pager,
    RoleDeleteDialog,
    Select,
    TableRow,
    TextField,
  ],
  host: { class: 'flex flex-col gap-6 sm:gap-8' },
  templateUrl: './roles-page.html',
})
export class RolesPage {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toasts = inject(ToastStore);
  private readonly injector = inject(Injector);
  private readonly heading = viewChild.required<ElementRef<HTMLElement>>('heading');
  private readonly params = toSignal(this.route.queryParamMap, { requireSync: true });

  protected readonly access = inject(RoleAccess);
  protected readonly section = adminSection('roles');
  protected readonly typeOptions = TYPE_OPTIONS;
  protected readonly columns: readonly TableColumn[] = [
    { label: 'Name', sortField: 'name', sticky: 'start' },
    { label: 'Description' },
    { label: 'Permissions' },
    { label: 'Type', sortField: 'is_default' },
    { label: 'Created', sortField: 'created_at' },
    { label: 'Actions', sticky: 'end' },
  ];

  protected readonly query = computed<RoleQuery>(() => {
    const params = this.params();
    const page = Number.parseInt(params.get('page') ?? '', 10);
    const sort = ROLE_SORT_FIELDS.find((field) => field === params.get('sort'));
    const type = ROLE_TYPES.find((candidate) => candidate === params.get('type'));
    return {
      name: params.get('q')?.trim() ?? '',
      type: type ?? null,
      page: Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE,
      sort: sort ?? DEFAULT_SORT,
      direction: params.get('direction') === 'desc' ? 'desc' : 'asc',
    };
  });
  protected readonly sort = computed<TableSort>(() => ({
    field: this.query().sort,
    direction: this.query().direction,
  }));

  protected readonly roles = httpResource<Page<AdminRole>>(() => rolesListRequest(this.query()));
  protected readonly shownRoles = lastValue(this.roles);
  protected readonly rows = computed(() => this.shownRoles()?.data ?? []);
  protected readonly emptyText = computed(() => {
    const query = this.query();
    if (query.name !== '' || query.type !== null) {
      return 'No roles match these filters';
    }
    if (query.page > FIRST_PAGE) {
      return 'This page is empty';
    }
    return 'No roles yet';
  });

  protected readonly deleteTarget = signal<AdminRole | null>(null);

  private searchedName: string | null = null;
  private readonly nameParam = computed(() => this.query().name);
  private readonly searchModel = linkedSignal<string, SearchModel>({
    source: this.nameParam,
    computation: (name, previous) => {
      const echoed = name === this.searchedName;
      this.searchedName = null;
      return previous && (echoed || previous.value.name.trim() === name)
        ? previous.value
        : { name };
    },
  });
  protected readonly searchForm = form(this.searchModel, {
    submission: {
      action: async (field) => {
        await this.search(field().value().name);
        return undefined;
      },
    },
  });

  constructor() {
    toObservable(computed(() => this.searchModel().name))
      .pipe(debounceTime(SEARCH_DEBOUNCE_MS), takeUntilDestroyed())
      .subscribe((name) => void this.search(name));
  }

  protected detailPath(role: AdminRole): string {
    return `${this.section.path}/${role.id}`;
  }

  protected newPath(): string {
    return `${this.section.path}/new`;
  }

  protected openDelete(role: AdminRole): void {
    this.deleteTarget.set(role);
  }

  protected onTypeFilter(type: string): void {
    void this.navigate({ type: type === '' ? null : type, page: null });
  }

  protected onSort(sort: TableSort): void {
    void this.navigate({
      sort: sort.field === DEFAULT_SORT ? null : sort.field,
      direction: sort.direction === 'asc' ? null : sort.direction,
      page: null,
    });
  }

  protected onDeleted(role: AdminRole): void {
    this.deleteTarget.set(null);
    this.toasts.show(`Role “${role.name}” deleted`);
    this.refreshAfterRemoval();
  }

  protected onGone(): void {
    this.deleteTarget.set(null);
    this.toasts.show('That role no longer exists', 'error');
    this.refreshAfterRemoval();
  }

  private refreshAfterRemoval(): void {
    afterNextRender(() => this.heading().nativeElement.focus(), { injector: this.injector });
    const page = this.shownRoles();
    if (page && page.page > FIRST_PAGE && page.data.length <= 1) {
      const previous = page.page - 1;
      void this.navigate({ page: previous === FIRST_PAGE ? null : String(previous) });
      return;
    }
    this.roles.reload();
  }

  private search(name: string): Promise<boolean> {
    const trimmed = name.trim();
    if (trimmed === this.query().name) {
      return Promise.resolve(true);
    }
    this.searchedName = trimmed;
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

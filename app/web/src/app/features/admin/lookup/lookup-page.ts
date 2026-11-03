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
import { ActivatedRoute, Router } from '@angular/router';
import { LucidePencil, LucidePlus, LucideTrash } from '@lucide/angular';
import { debounceTime } from 'rxjs';
import { adminSection } from '../../../core/admin/admin-sections';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { DataTable, type TableColumn, TableRow, type TableSort } from '../../../ui/data-table';
import { FIRST_PAGE, Pager } from '../../../ui/pager';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { AdminHeading } from '../admin-heading';
import {
  LOOKUP_SORT_FIELDS,
  type LookupItem,
  type LookupQuery,
  type LookupSortField,
  lookupListRequest,
} from './lookup-api';
import { capitalized, LOOKUP_DATA_KEY, type LookupConfig } from './lookup-config';
import { LookupDeleteDialog } from './lookup-delete-dialog';
import { LookupFormDialog, type LookupSaved } from './lookup-form-dialog';

type SearchModel = {
  name: string;
};

type FormTarget = {
  item: LookupItem | null;
};

const SEARCH_DEBOUNCE_MS = 300;
const DEFAULT_SORT: LookupSortField = 'name';

@Component({
  selector: 'cc-lookup-page',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    LucidePencil,
    LucidePlus,
    LucideTrash,
    AdminHeading,
    Alert,
    Button,
    DataTable,
    LookupDeleteDialog,
    LookupFormDialog,
    Pager,
    TableRow,
    TextField,
  ],
  host: { class: 'flex flex-col gap-6 sm:gap-8' },
  templateUrl: './lookup-page.html',
})
export class LookupPage {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toasts = inject(ToastStore);
  private readonly injector = inject(Injector);
  private readonly newButton = viewChild.required<ElementRef<HTMLButtonElement>>('newButton');
  private readonly params = toSignal(this.route.queryParamMap, { requireSync: true });

  protected readonly config = this.route.snapshot.data[LOOKUP_DATA_KEY] as LookupConfig;
  protected readonly section = adminSection(this.config.section);
  protected readonly label = capitalized(this.config.noun);
  protected readonly columns: readonly TableColumn[] = [
    { label: 'Name', sortField: 'name', sticky: 'start' },
    { label: 'Description' },
    { label: 'Created', sortField: 'created_at' },
    { label: 'Updated', sortField: 'updated_at' },
    { label: 'Actions', sticky: 'end' },
  ];

  protected readonly query = computed<LookupQuery>(() => {
    const params = this.params();
    const page = Number.parseInt(params.get('page') ?? '', 10);
    const sort = LOOKUP_SORT_FIELDS.find((field) => field === params.get('sort'));
    return {
      name: params.get('q')?.trim() ?? '',
      page: Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE,
      sort: sort ?? DEFAULT_SORT,
      direction: params.get('direction') === 'desc' ? 'desc' : 'asc',
    };
  });
  protected readonly sort = computed<TableSort>(() => ({
    field: this.query().sort,
    direction: this.query().direction,
  }));

  protected readonly items = httpResource<Page<LookupItem>>(() =>
    lookupListRequest(this.config.url, this.query()),
  );
  protected readonly shownItems = lastValue(this.items);
  protected readonly rows = computed(() => this.shownItems()?.data ?? []);
  protected readonly emptyText = computed(() => {
    const query = this.query();
    if (query.name !== '') {
      return `No ${this.section.label.toLowerCase()} match “${query.name}”`;
    }
    if (query.page > FIRST_PAGE) {
      return 'This page is empty';
    }
    return `No ${this.section.label.toLowerCase()} yet`;
  });

  protected readonly formTarget = signal<FormTarget | null>(null);
  protected readonly deleteTarget = signal<LookupItem | null>(null);

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

  protected openCreate(): void {
    this.formTarget.set({ item: null });
  }

  protected openEdit(item: LookupItem): void {
    this.formTarget.set({ item });
  }

  protected openDelete(item: LookupItem): void {
    this.deleteTarget.set(item);
  }

  protected onSort(sort: TableSort): void {
    void this.navigate({
      sort: sort.field === DEFAULT_SORT ? null : sort.field,
      direction: sort.direction === 'asc' ? null : sort.direction,
      page: null,
    });
  }

  protected onSaved(saved: LookupSaved): void {
    this.formTarget.set(null);
    this.toasts.show(`${this.label} “${saved.item.name}” ${saved.created ? 'created' : 'updated'}`);
    this.items.reload();
  }

  protected onDeleted(item: LookupItem): void {
    this.deleteTarget.set(null);
    this.toasts.show(`${this.label} “${item.name}” deleted`);
    this.refreshAfterRemoval();
  }

  protected onGone(): void {
    this.formTarget.set(null);
    this.deleteTarget.set(null);
    this.toasts.show(`That ${this.config.noun} no longer exists`, 'error');
    this.refreshAfterRemoval();
  }

  private refreshAfterRemoval(): void {
    afterNextRender(() => this.newButton().nativeElement.focus(), { injector: this.injector });
    const page = this.shownItems();
    if (page && page.page > FIRST_PAGE && page.data.length <= 1) {
      const previous = page.page - 1;
      void this.navigate({ page: previous === FIRST_PAGE ? null : String(previous) });
      return;
    }
    this.items.reload();
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

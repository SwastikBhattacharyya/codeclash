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
import { LucideEye, LucidePencil, LucidePlus, LucideTrash } from '@lucide/angular';
import { debounceTime, groupBy, mergeMap, Subject } from 'rxjs';
import { adminSection } from '../../../core/admin/admin-sections';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import { CONTESTS_PATH, type ContestSummary } from '../../../core/contests/contests-api';
import { Clock, MINUTE_MS } from '../../../core/time/clock';
import { fromLocalInput, toLocalInput } from '../../../core/time/local-datetime';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { DataTable, type TableColumn, TableRow, type TableSort } from '../../../ui/data-table';
import { DateTimeField } from '../../../ui/datetime-field';
import { FIRST_PAGE, Pager } from '../../../ui/pager';
import { Select, type SelectOption } from '../../../ui/select';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { AdminHeading } from '../admin-heading';
import { ContestDeleteDialog } from './contest-delete-dialog';
import { ContestStatusBadge } from './contest-status-badge';
import {
  CONTEST_SORT_FIELDS,
  CONTEST_STATUS_LABELS,
  CONTEST_STATUSES,
  type ContestQuery,
  type ContestSortField,
  contestStarted,
  contestsListRequest,
} from './contests-api';

type SearchModel = {
  name: string;
};

type RangeParam = 'starts_from' | 'starts_to';

type RangeInput = {
  param: RangeParam;
  value: string;
};

const SEARCH_DEBOUNCE_MS = 300;
const RANGE_DEBOUNCE_MS = 500;
const INSTANT_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d{1,9})?)?Z$/;
const DEFAULT_SORT: ContestSortField = 'live_at';
const STATUS_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  ...CONTEST_STATUSES.map((status) => ({ value: status, label: CONTEST_STATUS_LABELS[status] })),
];

@Component({
  selector: 'cc-contests-page',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    RouterLink,
    LucideEye,
    LucidePencil,
    LucidePlus,
    LucideTrash,
    AdminHeading,
    Alert,
    Button,
    ContestDeleteDialog,
    ContestStatusBadge,
    DataTable,
    DateTimeField,
    Pager,
    Select,
    TableRow,
    TextField,
  ],
  host: { class: 'flex flex-col gap-6 sm:gap-8' },
  templateUrl: './contests-page.html',
})
export class ContestsPage {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toasts = inject(ToastStore);
  private readonly injector = inject(Injector);
  private readonly clock = inject(Clock);
  private readonly newButton = viewChild.required<ElementRef<HTMLElement>>('newButton');
  private readonly params = toSignal(this.route.queryParamMap, { requireSync: true });

  protected readonly section = adminSection('contests');
  protected readonly contestsPath = CONTESTS_PATH;
  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly columns: readonly TableColumn[] = [
    { label: 'Name', sortField: 'name', sticky: 'start' },
    { label: 'Status' },
    { label: 'Starts', sortField: 'live_at' },
    { label: 'Ends', sortField: 'end_at' },
    { label: 'Updated', sortField: 'updated_at' },
    { label: 'Actions', sticky: 'end' },
  ];

  protected readonly query = computed<ContestQuery>(() => {
    const params = this.params();
    const page = Number.parseInt(params.get('page') ?? '', 10);
    const sort = CONTEST_SORT_FIELDS.find((field) => field === params.get('sort'));
    const status = CONTEST_STATUSES.find((candidate) => candidate === params.get('status'));
    return {
      name: params.get('q')?.trim() ?? '',
      status: status ?? null,
      liveFrom: ContestsPage.instantOrNull(params.get('starts_from')),
      liveTo: ContestsPage.instantOrNull(params.get('starts_to')),
      page: Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE,
      sort: sort ?? DEFAULT_SORT,
      direction: params.get('direction') === 'asc' ? 'asc' : 'desc',
    };
  });
  protected readonly sort = computed<TableSort>(() => ({
    field: this.query().sort,
    direction: this.query().direction,
  }));
  protected readonly hasFilters = computed(() => {
    const query = this.query();
    return (
      query.name !== '' || query.status !== null || query.liveFrom !== null || query.liveTo !== null
    );
  });
  protected readonly liveFromInput = computed(() =>
    ContestsPage.localOrEmpty(this.query().liveFrom),
  );
  protected readonly liveToInput = computed(() => ContestsPage.localOrEmpty(this.query().liveTo));

  private readonly refreshes = signal(0);
  private readonly queriedAt = linkedSignal({
    source: () => ({ query: this.query(), refresh: this.refreshes() }),
    computation: () => new Date().toISOString(),
  });
  protected readonly contests = httpResource<Page<ContestSummary>>(() =>
    contestsListRequest(this.query(), this.queriedAt()),
  );
  protected readonly shownContests = lastValue(this.contests);
  protected readonly rows = computed(() => this.shownContests()?.data ?? []);
  protected readonly emptyText = computed(() => {
    if (this.hasFilters()) {
      return 'No contests match these filters';
    }
    if (this.query().page > FIRST_PAGE) {
      return 'This page is empty';
    }
    return 'No contests yet';
  });

  protected readonly deleteTarget = signal<ContestSummary | null>(null);

  private readonly rangeInputs = new Subject<RangeInput>();
  private readonly rangeDrafts: Record<RangeParam, string | null> = {
    starts_from: null,
    starts_to: null,
  };

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
    this.rangeInputs
      .pipe(
        groupBy((input) => input.param),
        mergeMap((inputs) => inputs.pipe(debounceTime(RANGE_DEBOUNCE_MS))),
        takeUntilDestroyed(),
      )
      .subscribe((input) => this.applyRange(input));
  }

  protected detailPath(contest: ContestSummary): string {
    return `${this.section.path}/${contest.id}`;
  }

  protected newPath(): string {
    return `${this.section.path}/new`;
  }

  protected started(contest: ContestSummary): boolean {
    return contestStarted(contest, this.clock.now());
  }

  protected openDelete(contest: ContestSummary): void {
    this.deleteTarget.set(contest);
  }

  protected onStatusFilter(status: string): void {
    void this.navigate({ status: status === '' ? null : status, page: null });
  }

  protected onRangeFilter(param: RangeParam, value: string): void {
    this.rangeDrafts[param] = value;
    if (value !== '') {
      this.rangeInputs.next({ param, value });
    }
  }

  protected onRangeBlur(param: RangeParam): void {
    if (this.rangeDrafts[param] === '') {
      this.rangeInputs.next({ param, value: '' });
    }
  }

  protected refresh(): void {
    this.refreshes.update((count) => count + 1);
  }

  protected onDeleteBlocked(): void {
    this.refresh();
  }

  protected onSort(sort: TableSort): void {
    void this.navigate({
      sort: sort.field === DEFAULT_SORT ? null : sort.field,
      direction: sort.direction === 'desc' ? null : sort.direction,
      page: null,
    });
  }

  protected onDeleted(contest: ContestSummary): void {
    this.deleteTarget.set(null);
    this.toasts.show(`Contest “${contest.name}” deleted`);
    this.refreshAfterRemoval();
  }

  protected onGone(): void {
    this.deleteTarget.set(null);
    this.toasts.show('That contest no longer exists', 'error');
    this.refreshAfterRemoval();
  }

  private refreshAfterRemoval(): void {
    afterNextRender(() => this.newButton().nativeElement.focus(), { injector: this.injector });
    const page = this.shownContests();
    if (page && page.page > FIRST_PAGE && page.data.length <= 1) {
      const previous = page.page - 1;
      void this.navigate({ page: previous === FIRST_PAGE ? null : String(previous) });
      return;
    }
    this.refresh();
  }

  private applyRange(input: RangeInput): void {
    const draft = this.rangeDrafts[input.param];
    if (draft !== null && draft !== input.value) {
      return;
    }
    this.rangeDrafts[input.param] = null;
    const current = input.param === 'starts_from' ? this.query().liveFrom : this.query().liveTo;
    const instant = input.value === '' ? null : fromLocalInput(input.value);
    if (input.value !== '' && instant === null) {
      return;
    }
    if (instant !== current) {
      void this.navigate({ [input.param]: instant, page: null }, true);
    }
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

  private static instantOrNull(value: string | null): string | null {
    if (value === null) {
      return null;
    }
    if (!INSTANT_PATTERN.test(value)) {
      return null;
    }
    const time = Date.parse(value);
    return Number.isNaN(time)
      ? null
      : new Date(Math.floor(time / MINUTE_MS) * MINUTE_MS).toISOString();
  }

  private static localOrEmpty(instant: string | null): string {
    return instant === null ? '' : toLocalInput(instant);
  }
}

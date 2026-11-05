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
import { debounceTime } from 'rxjs';
import { adminSection } from '../../../core/admin/admin-sections';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import {
  CATEGORIES_URL,
  type Category,
  lookupRequest,
  PROBLEM_PATH,
  TAGS_URL,
  type Tag,
} from '../../../core/problems/problems-api';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { DataTable, type TableColumn, TableRow, type TableSort } from '../../../ui/data-table';
import { FIRST_PAGE, Pager } from '../../../ui/pager';
import { Select, type SelectOption } from '../../../ui/select';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { AdminHeading } from '../admin-heading';
import { QuestionDeleteDialog } from './question-delete-dialog';
import {
  type AdminQuestion,
  QUESTION_SORT_FIELDS,
  QUESTION_STATUSES,
  type QuestionQuery,
  type QuestionSortField,
  questionsListRequest,
} from './questions-api';

type SearchModel = {
  title: string;
};

const SEARCH_DEBOUNCE_MS = 300;
const DEFAULT_SORT: QuestionSortField = 'updated_at';
const MS_PER_SECOND = 1000;
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const UNKNOWN_LABEL = 'Unknown';
const STATUS_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  { value: 'published', label: 'Published' },
  { value: 'draft', label: 'Draft' },
];

@Component({
  selector: 'cc-questions-page',
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
    DataTable,
    Pager,
    QuestionDeleteDialog,
    Select,
    TableRow,
    TextField,
  ],
  host: { class: 'flex flex-col gap-6 sm:gap-8' },
  templateUrl: './questions-page.html',
})
export class QuestionsPage {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toasts = inject(ToastStore);
  private readonly injector = inject(Injector);
  private readonly newButton = viewChild.required<ElementRef<HTMLElement>>('newButton');
  private readonly params = toSignal(this.route.queryParamMap, { requireSync: true });

  protected readonly section = adminSection('questions');
  protected readonly problemPath = PROBLEM_PATH;
  protected readonly statusOptions = STATUS_OPTIONS;
  protected readonly columns: readonly TableColumn[] = [
    { label: 'Title', sortField: 'title', sticky: 'start' },
    { label: 'Status', sortField: 'visible' },
    { label: 'Categories' },
    { label: 'Tags' },
    { label: 'Limits' },
    { label: 'Updated', sortField: 'updated_at' },
    { label: 'Actions', sticky: 'end' },
  ];

  protected readonly query = computed<QuestionQuery>(() => {
    const params = this.params();
    const page = Number.parseInt(params.get('page') ?? '', 10);
    const sort = QUESTION_SORT_FIELDS.find((field) => field === params.get('sort'));
    const status = QUESTION_STATUSES.find((candidate) => candidate === params.get('status'));
    return {
      title: params.get('q')?.trim() ?? '',
      status: status ?? null,
      category: QuestionsPage.uuidOrNull(params.get('category')),
      tag: QuestionsPage.uuidOrNull(params.get('tag')),
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
      query.title !== '' || query.status !== null || query.category !== null || query.tag !== null
    );
  });

  protected readonly questions = httpResource<Page<AdminQuestion>>(() =>
    questionsListRequest(this.query()),
  );
  protected readonly shownQuestions = lastValue(this.questions);
  protected readonly rows = computed(() => this.shownQuestions()?.data ?? []);
  protected readonly emptyText = computed(() => {
    if (this.hasFilters()) {
      return 'No questions match these filters';
    }
    if (this.query().page > FIRST_PAGE) {
      return 'This page is empty';
    }
    return 'No questions yet';
  });

  protected readonly categories = httpResource<Page<Category>>(() => lookupRequest(CATEGORIES_URL));
  protected readonly tags = httpResource<Page<Tag>>(() => lookupRequest(TAGS_URL));
  private readonly categoryNames = computed(
    () =>
      new Map(
        (this.categories.hasValue() ? this.categories.value().data : []).map((category) => [
          category.id,
          category.name,
        ]),
      ),
  );
  private readonly tagNames = computed(
    () =>
      new Map(
        (this.tags.hasValue() ? this.tags.value().data : []).map((tag) => [tag.id, tag.name]),
      ),
  );
  protected readonly categoryOptions = computed(() =>
    QuestionsPage.filterOptions('All categories', this.categoryNames(), this.query().category),
  );
  protected readonly tagOptions = computed(() =>
    QuestionsPage.filterOptions('All tags', this.tagNames(), this.query().tag),
  );

  protected readonly deleteTarget = signal<AdminQuestion | null>(null);

  private searchedTitle: string | null = null;
  private readonly titleParam = computed(() => this.query().title);
  private readonly searchModel = linkedSignal<string, SearchModel>({
    source: this.titleParam,
    computation: (title, previous) => {
      const echoed = title === this.searchedTitle;
      this.searchedTitle = null;
      return previous && (echoed || previous.value.title.trim() === title)
        ? previous.value
        : { title };
    },
  });
  protected readonly searchForm = form(this.searchModel, {
    submission: {
      action: async (field) => {
        await this.search(field().value().title);
        return undefined;
      },
    },
  });

  constructor() {
    toObservable(computed(() => this.searchModel().title))
      .pipe(debounceTime(SEARCH_DEBOUNCE_MS), takeUntilDestroyed())
      .subscribe((title) => void this.search(title));
  }

  protected detailPath(question: AdminQuestion): string {
    return `${this.section.path}/${question.id}`;
  }

  protected newPath(): string {
    return `${this.section.path}/new`;
  }

  protected categoryLabels(question: AdminQuestion): string[] {
    const names = this.categoryNames();
    return question.categories.map((id) => names.get(id) ?? UNKNOWN_LABEL);
  }

  protected tagLabels(question: AdminQuestion): string[] {
    const names = this.tagNames();
    return question.tags.map((id) => names.get(id) ?? UNKNOWN_LABEL);
  }

  protected limits(question: AdminQuestion): string {
    return `${question.maxRunTimeMs / MS_PER_SECOND} s · ${question.maxRamLimitMb} MB`;
  }

  protected openDelete(question: AdminQuestion): void {
    this.deleteTarget.set(question);
  }

  protected onStatusFilter(status: string): void {
    void this.navigate({ status: status === '' ? null : status, page: null });
  }

  protected onCategoryFilter(category: string): void {
    void this.navigate({ category: category === '' ? null : category, page: null });
  }

  protected onTagFilter(tag: string): void {
    void this.navigate({ tag: tag === '' ? null : tag, page: null });
  }

  protected onSort(sort: TableSort): void {
    void this.navigate({
      sort: sort.field === DEFAULT_SORT ? null : sort.field,
      direction: sort.direction === 'desc' ? null : sort.direction,
      page: null,
    });
  }

  protected onDeleted(question: AdminQuestion): void {
    this.deleteTarget.set(null);
    this.toasts.show(`Question “${question.title}” deleted`);
    this.refreshAfterRemoval();
  }

  protected onGone(): void {
    this.deleteTarget.set(null);
    this.toasts.show('That question no longer exists', 'error');
    this.refreshAfterRemoval();
  }

  private refreshAfterRemoval(): void {
    afterNextRender(() => this.newButton().nativeElement.focus(), { injector: this.injector });
    const page = this.shownQuestions();
    if (page && page.page > FIRST_PAGE && page.data.length <= 1) {
      const previous = page.page - 1;
      void this.navigate({ page: previous === FIRST_PAGE ? null : String(previous) });
      return;
    }
    this.questions.reload();
  }

  private search(title: string): Promise<boolean> {
    const trimmed = title.trim();
    if (trimmed === this.query().title) {
      return Promise.resolve(true);
    }
    this.searchedTitle = trimmed;
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

  private static uuidOrNull(value: string | null): string | null {
    return value !== null && UUID_PATTERN.test(value) ? value : null;
  }

  private static filterOptions(
    allLabel: string,
    names: ReadonlyMap<string, string>,
    selected: string | null,
  ): SelectOption[] {
    const options = [...names.entries()].map(([value, label]) => ({ value, label }));
    if (selected !== null && !names.has(selected)) {
      options.unshift({ value: selected, label: UNKNOWN_LABEL });
    }
    return [{ value: '', label: allLabel }, ...options];
  }
}

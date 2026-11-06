import { DatePipe, isPlatformBrowser } from '@angular/common';
import { httpResource } from '@angular/common/http';
import {
  afterNextRender,
  Component,
  computed,
  DOCUMENT,
  type ElementRef,
  effect,
  Injector,
  inject,
  linkedSignal,
  PLATFORM_ID,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormField, FormRoot, form } from '@angular/forms/signals';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideCheck, LucideEye, LucideMinus, LucideTrash, LucideX } from '@lucide/angular';
import { debounceTime } from 'rxjs';
import { adminSection } from '../../../core/admin/admin-sections';
import { isGone } from '../../../core/api/gone';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import {
  type Question,
  questionsByIdsRequest,
  questionUrl,
} from '../../../core/problems/problems-api';
import {
  formatMemory,
  formatTime,
  isPending,
  LANGUAGE_LABELS,
  STATE_LABELS,
  SUBMISSION_LANGUAGES,
  SUBMISSION_STATES,
  type Submission,
  submissionUrl,
} from '../../../core/submissions/submissions';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { DataTable, type TableColumn, TableRow, type TableSort } from '../../../ui/data-table';
import { FIRST_PAGE, Pager } from '../../../ui/pager';
import { Select, type SelectOption } from '../../../ui/select';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { VerdictBadge } from '../../../ui/verdict-badge';
import { AdminHeading } from '../admin-heading';
import { SubmissionDeleteDialog } from './submission-delete-dialog';
import { SubmissionDialog } from './submission-dialog';
import {
  SUBMISSION_SORT_FIELDS,
  type SubmissionQuery,
  type SubmissionSortField,
  submissionsRequest,
} from './submissions-api';
import {
  type BulkDeleteResult,
  SubmissionsBulkDeleteDialog,
} from './submissions-bulk-delete-dialog';

type SearchModel = {
  username: string;
};

type SelectAllState = 'all' | 'some' | 'none';

const SEARCH_DEBOUNCE_MS = 300;
const POLL_INTERVAL_MS = 5000;
const DEFAULT_SORT: SubmissionSortField = 'created_at';
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const STATE_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  ...SUBMISSION_STATES.map((state) => ({ value: state, label: STATE_LABELS[state] })),
];
const LANGUAGE_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  ...SUBMISSION_LANGUAGES.map((language) => ({
    value: language,
    label: LANGUAGE_LABELS[language],
  })),
];

@Component({
  selector: 'cc-submissions-page',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    LucideCheck,
    LucideEye,
    LucideMinus,
    LucideTrash,
    LucideX,
    AdminHeading,
    Alert,
    Button,
    DataTable,
    Pager,
    Select,
    SubmissionDeleteDialog,
    SubmissionDialog,
    SubmissionsBulkDeleteDialog,
    TableRow,
    TextField,
    VerdictBadge,
  ],
  host: { class: 'flex flex-col gap-6 sm:gap-8' },
  templateUrl: './submissions-page.html',
})
export class SubmissionsPage {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly document = inject(DOCUMENT);
  private readonly toasts = inject(ToastStore);
  private readonly injector = inject(Injector);
  private readonly heading = viewChild.required<ElementRef<HTMLElement>>('heading');
  private readonly params = toSignal(this.route.queryParamMap, { requireSync: true });

  protected readonly section = adminSection('submissions');
  protected readonly stateOptions = STATE_OPTIONS;
  protected readonly languageOptions = LANGUAGE_OPTIONS;
  protected readonly languageLabels = LANGUAGE_LABELS;
  protected readonly formatTime = formatTime;
  protected readonly formatMemory = formatMemory;
  protected readonly columns: readonly TableColumn[] = [
    { label: 'Select' },
    { label: 'User', sticky: 'start' },
    { label: 'Question' },
    { label: 'Language', sortField: 'language' },
    { label: 'Verdict', sortField: 'state' },
    { label: 'Time' },
    { label: 'Memory' },
    { label: 'Submitted', sortField: 'created_at' },
    { label: 'Actions', sticky: 'end' },
  ];

  protected readonly query = computed<SubmissionQuery>(() => {
    const params = this.params();
    const page = Number.parseInt(params.get('page') ?? '', 10);
    const sort = SUBMISSION_SORT_FIELDS.find((field) => field === params.get('sort'));
    const state = SUBMISSION_STATES.find((candidate) => candidate === params.get('state'));
    const language = SUBMISSION_LANGUAGES.find((candidate) => candidate === params.get('language'));
    const questionId = params.get('question_id');
    return {
      username: params.get('username')?.trim() ?? '',
      questionId: questionId !== null && UUID_PATTERN.test(questionId) ? questionId : null,
      state: state ?? null,
      language: language ?? null,
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
      query.username !== '' ||
      query.questionId !== null ||
      query.state !== null ||
      query.language !== null
    );
  });

  protected readonly submissions = httpResource<Page<Submission>>(() =>
    submissionsRequest(this.query()),
  );
  protected readonly shownSubmissions = lastValue(this.submissions);
  protected readonly rows = computed(() => this.shownSubmissions()?.data ?? []);
  protected readonly emptyText = computed(() => {
    if (this.hasFilters()) {
      return 'No submissions match these filters';
    }
    if (this.query().page > FIRST_PAGE) {
      return 'This page is empty';
    }
    return 'No submissions yet';
  });

  private readonly questionIds = computed(
    () => [...new Set(this.rows().map((submission) => submission.questionId))].sort(),
    { equal: (left, right) => left.join() === right.join() },
  );
  private readonly questions = httpResource<Page<Question>>(() => {
    const ids = this.questionIds();
    return ids.length > 0 ? questionsByIdsRequest(ids) : undefined;
  });
  private readonly questionTitles = computed(
    () =>
      new Map(
        (this.questions.hasValue() ? this.questions.value().data : []).map((question) => [
          question.id,
          question.title,
        ]),
      ),
  );
  private readonly filterQuestionId = computed(() => this.query().questionId);
  private readonly filterQuestion = httpResource<Question>(() => {
    const id = this.filterQuestionId();
    return id ? questionUrl(id) : undefined;
  });
  protected readonly filterQuestionTitle = computed(() =>
    this.filterQuestion.hasValue() ? this.filterQuestion.value().title : 'Selected question',
  );

  protected readonly selected = linkedSignal<SubmissionQuery, ReadonlySet<string>>({
    source: this.query,
    computation: () => new Set(),
  });
  protected readonly selectedSubmissions = computed(() =>
    this.rows().filter((submission) => this.selected().has(submission.id)),
  );
  protected readonly selectAllState = computed<SelectAllState>(() => {
    const count = this.selectedSubmissions().length;
    if (count === 0) {
      return 'none';
    }
    return count === this.rows().length ? 'all' : 'some';
  });

  protected readonly viewId = signal<string | null>(null);
  private readonly viewSnapshot = signal<Submission | null>(null);
  private readonly viewResource = httpResource<Submission>(() => {
    const id = this.viewId();
    return id ? submissionUrl(id) : undefined;
  });
  protected readonly viewed = computed(() => {
    const id = this.viewId();
    if (id === null) {
      return null;
    }
    if (this.viewResource.hasValue()) {
      return this.viewResource.value();
    }
    if (isGone(this.viewResource.error())) {
      return null;
    }
    return this.rows().find((submission) => submission.id === id) ?? this.viewSnapshot();
  });
  protected readonly deleteTarget = signal<Submission | null>(null);
  protected readonly bulkTargets = signal<readonly Submission[] | null>(null);

  private readonly hasPending = computed(() => {
    const viewed = this.viewed();
    return (
      this.rows().some((submission) => isPending(submission.state)) ||
      (viewed !== null && isPending(viewed.state))
    );
  });

  private searchedUsername: string | null = null;
  private readonly usernameParam = computed(() => this.query().username);
  private readonly searchModel = linkedSignal<string, SearchModel>({
    source: this.usernameParam,
    computation: (username, previous) => {
      const echoed = username === this.searchedUsername;
      this.searchedUsername = null;
      return previous && (echoed || previous.value.username.trim() === username)
        ? previous.value
        : { username };
    },
  });
  protected readonly searchForm = form(this.searchModel, {
    submission: {
      action: async (field) => {
        await this.search(field().value().username);
        return undefined;
      },
    },
  });

  constructor() {
    toObservable(computed(() => this.searchModel().username))
      .pipe(debounceTime(SEARCH_DEBOUNCE_MS), takeUntilDestroyed())
      .subscribe((username) => void this.search(username));

    const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
    effect((onCleanup) => {
      if (!isBrowser || !this.hasPending()) {
        return;
      }
      const handle = setInterval(() => {
        if (this.document.visibilityState !== 'visible') {
          return;
        }
        if (!this.submissions.isLoading()) {
          this.submissions.reload();
        }
        const viewed = this.viewed();
        if (viewed !== null && isPending(viewed.state) && !this.viewResource.isLoading()) {
          this.viewResource.reload();
        }
      }, POLL_INTERVAL_MS);
      onCleanup(() => clearInterval(handle));
    });
  }

  protected questionTitle(submission: Submission): string | null {
    return this.questionTitles().get(submission.questionId) ?? null;
  }

  protected isSelected(submission: Submission): boolean {
    return this.selected().has(submission.id);
  }

  protected toggleSelected(submission: Submission, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.selected.update((current) => {
      const next = new Set(current);
      if (checked) {
        next.add(submission.id);
      } else {
        next.delete(submission.id);
      }
      return next;
    });
  }

  protected toggleAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.selected.set(
      checked ? new Set(this.rows().map((submission) => submission.id)) : new Set(),
    );
  }

  protected openView(submission: Submission): void {
    this.viewSnapshot.set(submission);
    this.viewId.set(submission.id);
  }

  protected closeView(): void {
    this.viewId.set(null);
    this.viewSnapshot.set(null);
  }

  protected openBulkDelete(): void {
    this.bulkTargets.set(this.selectedSubmissions());
  }

  protected requestDelete(submission: Submission): void {
    this.closeView();
    this.deleteTarget.set(submission);
  }

  protected onStateFilter(state: string): void {
    void this.navigate({ state: state === '' ? null : state, page: null });
  }

  protected onLanguageFilter(language: string): void {
    void this.navigate({ language: language === '' ? null : language, page: null });
  }

  protected clearQuestionFilter(): void {
    void this.navigate({ question_id: null, page: null });
  }

  protected onSort(sort: TableSort): void {
    void this.navigate({
      sort: sort.field === DEFAULT_SORT ? null : sort.field,
      direction: sort.direction === 'desc' ? null : sort.direction,
      page: null,
    });
  }

  protected onDeleted(): void {
    this.deleteTarget.set(null);
    this.toasts.show('Submission deleted');
    this.refreshAfterRemoval(1);
  }

  protected onGone(): void {
    this.deleteTarget.set(null);
    this.toasts.show('That submission no longer exists', 'error');
    this.refreshAfterRemoval(1);
  }

  protected onBulkFinished(result: BulkDeleteResult): void {
    this.bulkTargets.set(null);
    this.selected.set(new Set(result.failed));
    const deleted = result.deleted.length;
    const failed = result.failed.length;
    const summary = failed === 0 ? `${deleted} deleted` : `${deleted} deleted, ${failed} failed`;
    this.toasts.show(summary, failed === 0 ? 'success' : 'error');
    this.refreshAfterRemoval(deleted);
  }

  private refreshAfterRemoval(removed: number): void {
    afterNextRender(() => this.heading().nativeElement.focus(), { injector: this.injector });
    const page = this.shownSubmissions();
    if (page && page.page > FIRST_PAGE && removed > 0 && page.data.length <= removed) {
      const previous = page.page - 1;
      void this.navigate({ page: previous === FIRST_PAGE ? null : String(previous) });
      return;
    }
    this.submissions.reload();
  }

  private search(username: string): Promise<boolean> {
    const trimmed = username.trim();
    if (trimmed === this.query().username) {
      return Promise.resolve(true);
    }
    this.searchedUsername = trimmed;
    return this.navigate({ username: trimmed === '' ? null : trimmed, page: null }, true);
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

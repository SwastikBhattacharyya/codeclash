import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import {
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  signal,
  untracked,
} from '@angular/core';
import { LucideCode } from '@lucide/angular';
import type { Page } from '../../core/api/page';
import { pollWhile } from '../../core/api/poll';
import { AuthStore } from '../../core/auth/auth-store';
import {
  formatMemory,
  formatTime,
  isPending,
  LANGUAGE_LABELS,
  type Submission,
  type SubmissionLanguage,
  submissionHistoryRequest,
} from '../../core/submissions/submissions';
import { Alert } from '../../ui/alert';
import { Button } from '../../ui/button';
import { CodeView } from '../../ui/code-view';
import { CompileOutput } from '../../ui/compile-output';
import { DataTable, type TableColumn, TableRow } from '../../ui/data-table';
import { Dialog } from '../../ui/dialog';
import { FIRST_PAGE } from '../../ui/pager';
import { VerdictBadge } from '../../ui/verdict-badge';

export type LoadedCode = {
  code: string;
  language: SubmissionLanguage;
};

const HISTORY_PAGE_SIZE = 10;
const HISTORY_POLL_MS = 3000;
const POLL_RECENT_MS = 600_000;

type Scope = {
  questionId: string;
  everyone: boolean;
  userId: string | null;
};

@Component({
  selector: 'cc-submission-history',
  imports: [
    DatePipe,
    LucideCode,
    Alert,
    Button,
    CodeView,
    CompileOutput,
    DataTable,
    Dialog,
    TableRow,
    VerdictBadge,
  ],
  host: { class: 'flex flex-col gap-3' },
  templateUrl: './submission-history.html',
})
export class SubmissionHistory {
  private readonly auth = inject(AuthStore);

  readonly questionId = input.required<string>();
  readonly createdAfter = input<string | null>(null);
  readonly createdBefore = input<string | null>(null);
  readonly refresh = input(0);
  readonly highlightId = input<string | null>(null);
  readonly loadCode = output<LoadedCode>();

  protected readonly firstPage = FIRST_PAGE;
  protected readonly languageLabels = LANGUAGE_LABELS;
  protected readonly formatTime = formatTime;
  protected readonly formatMemory = formatMemory;
  protected readonly signedIn = this.auth.isAuthenticated;
  private staleRefresh = false;
  private readonly userId = computed(() => this.auth.session()?.userId ?? null);
  protected readonly everyone = linkedSignal(() => this.userId() === null);
  private readonly scope = computed<Scope>(
    () => ({
      questionId: this.questionId(),
      everyone: this.everyone(),
      userId: this.userId(),
    }),
    {
      equal: (left, right) =>
        left.questionId === right.questionId &&
        left.everyone === right.everyone &&
        left.userId === right.userId,
    },
  );
  protected readonly page = linkedSignal<Scope, number>({
    source: this.scope,
    computation: () => FIRST_PAGE,
  });
  protected readonly submissions = httpResource<Page<Submission>>(() => {
    const scope = this.scope();
    if (!scope.everyone && scope.userId === null) {
      return undefined;
    }
    return submissionHistoryRequest({
      questionId: scope.questionId,
      userId: scope.everyone ? null : scope.userId,
      createdAfter: this.createdAfter(),
      createdBefore: this.createdBefore(),
      page: this.page(),
      size: HISTORY_PAGE_SIZE,
    });
  });
  protected readonly shown = linkedSignal<
    { scope: Scope; page: Page<Submission> | undefined },
    { scope: Scope; page: Page<Submission> | undefined }
  >({
    source: () => ({
      scope: this.scope(),
      page: this.submissions.hasValue() ? this.submissions.value() : undefined,
    }),
    computation: (next, previous) =>
      next.page === undefined && previous !== undefined && previous.value.scope === next.scope
        ? previous.value
        : next,
  });
  protected readonly rows = computed(() => this.shown().page?.data ?? []);
  protected readonly columns = computed<readonly TableColumn[]>(() => [
    { label: 'Verdict', sticky: 'start' },
    ...(this.everyone() ? [{ label: 'User' }] : []),
    { label: 'Language' },
    { label: 'Time' },
    { label: 'Memory' },
    { label: 'Submitted' },
    { label: 'Code', sticky: 'end' },
  ]);
  protected readonly emptyText = computed(() =>
    this.everyone() ? 'No submissions yet' : "You haven't submitted to this problem yet",
  );
  protected readonly viewed = signal<Submission | null>(null);
  protected readonly viewedLanguage = computed(() => this.viewed()?.language ?? 'cpp');

  constructor() {
    pollWhile(
      this.submissions,
      () => this.rows().some((submission) => this.needsPolling(submission)),
      () => HISTORY_POLL_MS,
    );
    let seen: number | null = null;
    effect(() => {
      const refresh = this.refresh();
      if (seen !== null && seen !== refresh) {
        untracked(() => {
          this.staleRefresh = !this.submissions.reload();
        });
      }
      seen = refresh;
    });
    effect(() => {
      if (!this.submissions.isLoading() && this.staleRefresh) {
        untracked(() => {
          this.staleRefresh = !this.submissions.reload();
        });
      }
    });
  }

  private needsPolling(submission: Submission): boolean {
    return (
      isPending(submission.state) &&
      submission.id !== this.highlightId() &&
      Date.now() - Date.parse(submission.createdAt) < POLL_RECENT_MS
    );
  }

  protected toggleEveryone(event: Event): void {
    if (this.userId() === null) {
      return;
    }
    this.everyone.set((event.target as HTMLInputElement).checked);
  }

  protected goToPage(page: number): void {
    this.page.set(Math.max(FIRST_PAGE, page));
  }

  protected open(submission: Submission): void {
    this.viewed.set(submission);
  }

  protected useCode(submission: Submission): void {
    this.viewed.set(null);
    this.loadCode.emit({ code: submission.code, language: submission.language });
  }
}

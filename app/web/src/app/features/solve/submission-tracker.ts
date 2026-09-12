import { HttpErrorResponse, httpResource } from '@angular/common/http';
import {
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  untracked,
} from '@angular/core';
import { LucideCheck, LucideX } from '@lucide/angular';
import { isFinalPollError, pollWhile } from '../../core/api/poll';
import { type PublicTestCase, testCaseUrl } from '../../core/problems/problems-api';
import {
  failsOnTestCase,
  formatMemory,
  formatTime,
  isPending,
  LANGUAGE_LABELS,
  STATE_LABELS,
  type Submission,
  type SubmissionState,
  submissionUrl,
  verdictTone,
} from '../../core/submissions/submissions';
import { Clock } from '../../core/time/clock';
import { CompileOutput } from '../../ui/compile-output';
import { FieldIds } from '../../ui/field-ids';
import { Spinner } from '../../ui/spinner';
import { TestResultPanel } from '../../ui/test-result-panel';

type StepStatus = 'waiting' | 'active' | 'done' | 'failed' | 'skipped';

type Step = {
  label: string;
  status: StepStatus;
};

type FailedTest =
  | { kind: 'none' }
  | { kind: 'loading' }
  | { kind: 'public'; testCase: PublicTestCase }
  | { kind: 'hidden' }
  | { kind: 'removed' }
  | { kind: 'unknown' };

const FAST_POLL_MS = 1000;
const SLOW_POLL_MS = 3000;
const FAST_POLL_WINDOW_MS = 30_000;
const STALLED_AFTER_MS = 120_000;
const FORBIDDEN_STATUS = 403;
const NOT_FOUND_STATUS = 404;
const STEP_LABELS = ['Queued', 'Compiling', 'Running tests', 'Verdict'] as const;

@Component({
  selector: 'cc-submission-tracker',
  imports: [LucideCheck, LucideX, CompileOutput, Spinner, TestResultPanel],
  host: { class: 'block' },
  styleUrl: './submission-tracker.css',
  templateUrl: './submission-tracker.html',
})
export class SubmissionTracker {
  private readonly clock = inject(Clock);

  readonly submission = input.required<Submission>();
  readonly finished = output<Submission>();
  readonly lost = output<Submission>();

  protected readonly headingId = inject(FieldIds).create('submission-tracker');

  private readonly submissionId = computed(() => this.submission().id);
  private readonly startedAt = linkedSignal(() => {
    const created = Date.parse(this.submission().createdAt);
    const now = Date.now();
    return Number.isFinite(created) ? Math.min(created, now) : now;
  });
  private readonly latest = httpResource<Submission>(() => submissionUrl(this.submissionId()));
  protected readonly current = linkedSignal<Submission | null, Submission>({
    source: () => (this.latest.hasValue() ? this.latest.value() : null),
    computation: (fetched, previous) => {
      const id = this.submissionId();
      if (fetched !== null && fetched.id === id) {
        return fetched;
      }
      return previous !== undefined && previous.value.id === id
        ? previous.value
        : this.submission();
    },
  });
  protected readonly pollLost = computed(() => isFinalPollError(this.latest.error()));
  protected readonly state = computed(() => this.current().state);
  protected readonly pending = computed(() => isPending(this.state()));
  protected readonly label = computed(() => STATE_LABELS[this.state()]);
  protected readonly tone = computed(() => verdictTone(this.state()));
  protected readonly languageLabel = computed(() => LANGUAGE_LABELS[this.current().language]);
  protected readonly steps = computed(() => SubmissionTracker.stepsFor(this.state()));
  protected readonly stalled = computed(
    () =>
      this.pending() && !this.pollLost() && this.clock.now() - this.startedAt() > STALLED_AFTER_MS,
  );
  protected readonly celebrate = linkedSignal<string, boolean>({
    source: this.submissionId,
    computation: () => false,
  });
  protected readonly time = computed(() => formatTime(this.current().timeTakenMs));
  protected readonly memory = computed(() => formatMemory(this.current().memoryUsedKb));

  private readonly failedTestUrl = computed(() => {
    const submission = this.current();
    if (!failsOnTestCase(submission.state) || submission.failedTestCaseId === null) {
      return undefined;
    }
    return testCaseUrl(submission.questionId, submission.failedTestCaseId);
  });
  private readonly failedTestCase = httpResource<PublicTestCase>(() => this.failedTestUrl());
  protected readonly failedTest = computed<FailedTest>(() => {
    const submission = this.current();
    if (!failsOnTestCase(submission.state)) {
      return { kind: 'none' };
    }
    if (submission.failedTestCaseId === null) {
      return { kind: 'removed' };
    }
    if (this.failedTestCase.hasValue()) {
      return { kind: 'public', testCase: this.failedTestCase.value() };
    }
    const error = this.failedTestCase.error();
    if (error instanceof HttpErrorResponse && error.status === FORBIDDEN_STATUS) {
      return { kind: 'hidden' };
    }
    if (error instanceof HttpErrorResponse && error.status === NOT_FOUND_STATUS) {
      return { kind: 'removed' };
    }
    return error ? { kind: 'unknown' } : { kind: 'loading' };
  });

  private observedPending: string | null = null;
  private reported: string | null = null;
  private reportedLost: string | null = null;

  constructor() {
    pollWhile(
      this.latest,
      () => this.pending(),
      () => (Date.now() - this.startedAt() < FAST_POLL_WINDOW_MS ? FAST_POLL_MS : SLOW_POLL_MS),
    );
    effect(() => {
      const submission = this.current();
      untracked(() => this.observe(submission));
    });
    effect(() => {
      if (!this.pollLost()) {
        return;
      }
      untracked(() => {
        const submission = this.current();
        if (this.reportedLost !== submission.id) {
          this.reportedLost = submission.id;
          this.lost.emit(submission);
        }
      });
    });
  }

  private observe(submission: Submission): void {
    if (isPending(submission.state)) {
      this.observedPending = submission.id;
      return;
    }
    if (this.observedPending !== submission.id || this.reported === submission.id) {
      return;
    }
    this.reported = submission.id;
    if (submission.state === 'correct') {
      this.celebrate.set(true);
    }
    this.finished.emit(submission);
  }

  private static stepsFor(state: SubmissionState): Step[] {
    const statuses = SubmissionTracker.statuses(state);
    return STEP_LABELS.map((label, index) => ({ label, status: statuses[index] ?? 'waiting' }));
  }

  private static statuses(state: SubmissionState): StepStatus[] {
    switch (state) {
      case 'queued':
        return ['active', 'waiting', 'waiting', 'waiting'];
      case 'compiled':
        return ['done', 'done', 'active', 'waiting'];
      case 'compilation_error':
        return ['done', 'failed', 'skipped', 'failed'];
      case 'correct':
        return ['done', 'done', 'done', 'done'];
      case 'runner_error':
        return ['done', 'done', 'done', 'failed'];
      default:
        return ['done', 'done', 'failed', 'failed'];
    }
  }
}

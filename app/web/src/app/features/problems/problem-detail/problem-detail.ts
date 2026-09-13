import { httpResource } from '@angular/common/http';
import {
  Component,
  computed,
  effect,
  inject,
  linkedSignal,
  RESPONSE_INIT,
  signal,
  untracked,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { isNotFound } from '../../../core/api/not-found';
import type { Page } from '../../../core/api/page';
import { AuthStore } from '../../../core/auth/auth-store';
import {
  CONTEST_QUERY_PARAM,
  type Contest,
  contestPhase,
  contestUrl,
} from '../../../core/contests/contests-api';
import {
  CATEGORIES_URL,
  type Category,
  lookupRequest,
  PROBLEMS_PATH,
  type Question,
  questionUrl,
  TAGS_URL,
  type Tag,
} from '../../../core/problems/problems-api';
import {
  isPending,
  type Submission,
  submissionHistoryRequest,
} from '../../../core/submissions/submissions';
import { Clock } from '../../../core/time/clock';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Markdown } from '../../../ui/markdown';
import { FIRST_PAGE } from '../../../ui/pager';
import { Spinner } from '../../../ui/spinner';
import { SplitPane } from '../../../ui/split-pane';
import { ToastStore } from '../../../ui/toast-store';
import { SolvePanel } from '../../solve/solve-panel';
import { type LoadedCode, SubmissionHistory } from '../../solve/submission-history';
import { SubmissionTracker } from '../../solve/submission-tracker';
import { ContestBar } from './contest-bar';

const NOT_FOUND_STATUS = 404;
const MILLISECONDS_PER_SECOND = 1000;
const TITLE_SUFFIX = ' | CodeClash';
const SPLIT_STORAGE_KEY = 'codeclash:solve:split';
const LATEST_SIZE = 1;

@Component({
  selector: 'cc-problem-detail',
  imports: [
    RouterLink,
    Alert,
    Button,
    ContestBar,
    Markdown,
    SolvePanel,
    Spinner,
    SplitPane,
    SubmissionHistory,
    SubmissionTracker,
  ],
  host: { class: 'flex min-h-0 flex-1 flex-col' },
  templateUrl: './problem-detail.html',
})
export class ProblemDetail {
  private readonly title = inject(Title);
  private readonly responseInit = inject(RESPONSE_INIT);
  private readonly auth = inject(AuthStore);
  private readonly toasts = inject(ToastStore);
  private readonly clock = inject(Clock);
  private readonly route = inject(ActivatedRoute);
  private readonly params = toSignal(this.route.paramMap, { requireSync: true });
  private readonly queryParams = toSignal(this.route.queryParamMap, { requireSync: true });

  protected readonly problemsPath = PROBLEMS_PATH;
  protected readonly splitStorageKey = SPLIT_STORAGE_KEY;
  protected readonly questionId = computed(() => this.params().get('id'));
  protected readonly question = httpResource<Question>(() => {
    const id = this.questionId();
    return id ? questionUrl(id) : undefined;
  });
  private readonly contestId = computed(() => this.queryParams().get(CONTEST_QUERY_PARAM));
  protected readonly contest = httpResource<Contest>(() => {
    const id = this.contestId();
    return id ? contestUrl(id) : undefined;
  });
  protected readonly activeContest = computed<Contest | null>(() => {
    const contest = this.contest.hasValue() ? this.contest.value() : undefined;
    const questionId = this.questionId();
    if (!contest || !questionId || contestPhase(contest, this.clock.now()) !== 'live') {
      return null;
    }
    return contest.questions?.some((question) => question.id === questionId) ? contest : null;
  });
  protected readonly categories = httpResource<Page<Category>>(() => lookupRequest(CATEGORIES_URL));
  protected readonly tags = httpResource<Page<Tag>>(() => lookupRequest(TAGS_URL));
  protected readonly notFound = computed(() => isNotFound(this.question.error()));
  protected readonly categoryNames = computed(() => {
    const question = this.question.hasValue() ? this.question.value() : undefined;
    const names = new Map(
      (this.categories.hasValue() ? this.categories.value().data : []).map((item) => [
        item.id,
        item.name,
      ]),
    );
    return question ? question.categories.flatMap((id) => names.get(id) ?? []) : [];
  });
  protected readonly tagNames = computed(() => {
    const question = this.question.hasValue() ? this.question.value() : undefined;
    const names = new Map(
      (this.tags.hasValue() ? this.tags.value().data : []).map((item) => [item.id, item.name]),
    );
    return question ? question.tags.flatMap((id) => names.get(id) ?? []) : [];
  });

  private readonly userId = computed(() => this.auth.session()?.userId ?? null);
  private readonly viewer = computed(() => `${this.questionId() ?? ''}:${this.userId() ?? ''}`);
  protected readonly tracked = linkedSignal<string, Submission | null>({
    source: this.viewer,
    computation: () => null,
  });
  protected readonly judging = linkedSignal(() => {
    const submission = this.tracked();
    return submission !== null && isPending(submission.state);
  });
  protected readonly historyRefresh = signal(0);

  private readonly latestMine = httpResource<Page<Submission>>(() => {
    const questionId = this.questionId();
    const userId = this.userId();
    if (!questionId || userId === null) {
      return undefined;
    }
    return submissionHistoryRequest({
      questionId,
      userId,
      createdAfter: null,
      createdBefore: null,
      page: FIRST_PAGE,
      size: LATEST_SIZE,
    });
  });

  constructor() {
    effect(() => {
      const question = this.question.hasValue() ? this.question.value() : undefined;
      if (question) {
        this.title.setTitle(`${question.title}${TITLE_SUFFIX}`);
      }
      if (this.notFound() && this.responseInit) {
        this.responseInit.status = NOT_FOUND_STATUS;
      }
    });
    effect(() => {
      const latest = this.latestMine.hasValue() ? this.latestMine.value().data[0] : undefined;
      if (latest && isPending(latest.state)) {
        untracked(() => {
          if (this.tracked() === null && latest.questionId === this.questionId()) {
            this.tracked.set(latest);
          }
        });
      }
    });
  }

  protected seconds(milliseconds: number): string {
    return (milliseconds / MILLISECONDS_PER_SECOND).toLocaleString('en-US', {
      maximumFractionDigits: 2,
    });
  }

  protected onSubmitted(submission: Submission): void {
    this.tracked.set(submission);
    this.historyRefresh.update((count) => count + 1);
  }

  protected onFinished(submission: Submission): void {
    this.judging.set(false);
    if (submission.state === 'correct') {
      this.toasts.show('Accepted! All tests passed.');
    }
    this.historyRefresh.update((count) => count + 1);
  }

  protected onLost(): void {
    this.judging.set(false);
  }

  protected onRegistered(contest: Contest): void {
    this.contest.set({ ...contest, registered: true });
  }

  protected onLoadCode(panel: SolvePanel, loaded: LoadedCode): void {
    panel.load(loaded.code, loaded.language);
  }
}

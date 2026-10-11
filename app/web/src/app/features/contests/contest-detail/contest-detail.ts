import { DOCUMENT } from '@angular/common';
import { httpResource } from '@angular/common/http';
import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  type ElementRef,
  effect,
  Injector,
  inject,
  RESPONSE_INIT,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-errors';
import { lastValue } from '../../../core/api/last-value';
import { isNotFound } from '../../../core/api/not-found';
import type { Page } from '../../../core/api/page';
import { SIGN_IN_PATH } from '../../../core/auth/auth-paths';
import { AuthStore } from '../../../core/auth/auth-store';
import {
  CONTEST_QUERY_PARAM,
  CONTESTS_PATH,
  type Contest,
  type ContestPhase,
  ContestsApi,
  contestPhase,
  contestSolvedQuery,
  contestSolvedRequest,
  contestUrl,
  type LeaderboardEntry,
  leaderboardRequest,
  sameContestSolvedQuery,
} from '../../../core/contests/contests-api';
import {
  PROBLEM_PATH,
  type Question,
  questionsByIdsRequest,
} from '../../../core/problems/problems-api';
import type { Submission } from '../../../core/submissions/submissions';
import { Clock, describeDuration } from '../../../core/time/clock';
import { LocalTime } from '../../../core/time/local-time';
import { LucideCircleCheck } from '@lucide/angular';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Countdown } from '../../../ui/countdown';
import { Markdown } from '../../../ui/markdown';
import { Spinner } from '../../../ui/spinner';
import { ToastStore } from '../../../ui/toast-store';

const NOT_FOUND_STATUS = 404;
const LEADERBOARD_REFRESH_MS = 30_000;
const FIRST_PAGE = 1;
const TITLE_SUFFIX = ' | CodeClash';
const ID_SEPARATOR = ',';

type ContestProblem = {
  id: string;
  title: string;
  solved: boolean;
  points: number;
};

@Component({
  selector: 'cc-contest-detail',
  imports: [LocalTime, RouterLink, LucideCircleCheck, Alert, Button, Countdown, Markdown, Spinner],
  templateUrl: './contest-detail.html',
})
export class ContestDetail {
  private readonly api = inject(ContestsApi);
  private readonly clock = inject(Clock);
  private readonly toasts = inject(ToastStore);
  private readonly title = inject(Title);
  private readonly document = inject(DOCUMENT);
  private readonly injector = inject(Injector);
  private readonly responseInit = inject(RESPONSE_INIT);
  private readonly route = inject(ActivatedRoute);
  private readonly params = toSignal(this.route.paramMap, { requireSync: true });
  private readonly queryParams = toSignal(this.route.queryParamMap, { requireSync: true });
  private readonly registrationStatus = viewChild<ElementRef<HTMLElement>>('registrationStatus');
  private readonly registerButton = viewChild<ElementRef<HTMLElement>>('registerButton');
  private readonly cancelTrigger = viewChild<ElementRef<HTMLElement>>('cancelTrigger');
  private readonly keepButton = viewChild<ElementRef<HTMLElement>>('keepButton');

  protected readonly auth = inject(AuthStore);
  protected readonly contestsPath = CONTESTS_PATH;
  protected readonly problemPath = PROBLEM_PATH;
  protected readonly signInPath = SIGN_IN_PATH;
  protected readonly id = computed(() => this.params().get('id') ?? '');
  protected readonly leaderboardPage = computed(() => {
    const page = Number.parseInt(this.queryParams().get('page') ?? '', 10);
    return Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE;
  });

  protected readonly contest = httpResource<Contest>(() =>
    this.id() ? contestUrl(this.id()) : undefined,
  );
  protected readonly leaderboard = httpResource<Page<LeaderboardEntry>>(() =>
    this.id() ? leaderboardRequest(this.id(), this.leaderboardPage()) : undefined,
  );
  protected readonly shownContest = lastValue(this.contest);
  protected readonly shownLeaderboard = lastValue(this.leaderboard);
  private readonly questionIds = computed(
    () =>
      this.shownContest()
        ?.questions?.map((question) => question.id)
        .join(ID_SEPARATOR) ?? '',
  );
  protected readonly questionTitles = httpResource<Page<Question>>(() => {
    const ids = this.questionIds();
    return ids ? questionsByIdsRequest(ids.split(ID_SEPARATOR)) : undefined;
  });
  private readonly shownTitles = lastValue(this.questionTitles);
  private readonly userId = computed(() => this.auth.session()?.userId ?? null);
  private readonly solvedQuery = computed(
    () => contestSolvedQuery(this.userId(), this.shownContest()),
    { equal: sameContestSolvedQuery },
  );
  protected readonly solved = httpResource<Page<Submission>>(() =>
    contestSolvedRequest(this.solvedQuery()),
  );
  private readonly solvedIds = computed(
    () =>
      new Set(
        this.solved.hasValue()
          ? this.solved.value().data.map((submission) => submission.questionId)
          : [],
      ),
  );

  protected readonly notFound = computed(() => isNotFound(this.contest.error()));
  protected readonly phase = computed<ContestPhase | null>(() => {
    const contest = this.shownContest();
    return contest ? contestPhase(contest, this.clock.now()) : null;
  });
  protected readonly problems = computed<ContestProblem[]>(() => {
    const titles = new Map(
      (this.shownTitles()?.data ?? []).map((question) => [question.id, question.title]),
    );
    const solved = this.solvedIds();
    return (this.shownContest()?.questions ?? []).map((question) => ({
      id: question.id,
      points: question.points,
      title: titles.get(question.id) ?? 'Problem',
      solved: solved.has(question.id),
    }));
  });
  protected readonly problemParams = computed(() => ({ [CONTEST_QUERY_PARAM]: this.id() }));
  protected readonly signInParams = computed(() => ({
    redirect: `${CONTESTS_PATH}/${this.id()}`,
  }));
  protected readonly busy = signal(false);
  protected readonly confirmingUnregister = signal(false);

  constructor() {
    effect(() => {
      const contest = this.shownContest();
      if (contest) {
        this.title.setTitle(`${contest.name}${TITLE_SUFFIX}`);
      }
      if (this.notFound() && this.responseInit) {
        this.responseInit.status = NOT_FOUND_STATUS;
      }
    });

    let previousPhase: ContestPhase | null = null;
    effect(() => {
      const phase = this.phase();
      if (previousPhase !== null && phase !== null && phase !== previousPhase) {
        untracked(() => {
          this.contest.reload();
          this.leaderboard.reload();
        });
      }
      previousPhase = phase;
    });

    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const handle = setInterval(() => {
        if (this.phase() === 'live' && this.document.visibilityState === 'visible') {
          this.leaderboard.reload();
        }
      }, LEADERBOARD_REFRESH_MS);
      destroyRef.onDestroy(() => clearInterval(handle));
    });
  }

  protected duration(contest: Contest): string {
    return describeDuration(Date.parse(contest.endAt) - Date.parse(contest.liveAt));
  }

  protected leaderboardParams(page: number): Record<string, string | null> {
    return { page: page === FIRST_PAGE ? null : String(page) };
  }

  protected startUnregister(): void {
    this.confirmingUnregister.set(true);
    this.focusAfterRender(() => this.keepButton());
  }

  protected keepRegistration(): void {
    this.confirmingUnregister.set(false);
    this.focusAfterRender(() => this.cancelTrigger());
  }

  protected async register(): Promise<void> {
    if (await this.changeRegistration(true)) {
      this.focusAfterRender(() => this.registrationStatus());
    }
  }

  protected async unregister(): Promise<void> {
    if (await this.changeRegistration(false)) {
      this.confirmingUnregister.set(false);
      this.focusAfterRender(() => this.registerButton());
    }
  }

  private async changeRegistration(register: boolean): Promise<boolean> {
    const contest = this.shownContest();
    if (!contest || this.busy()) {
      return false;
    }
    this.busy.set(true);
    try {
      if (register) {
        await this.api.register(contest.id);
      } else {
        await this.api.unregister(contest.id);
      }
      this.contest.set({ ...contest, registered: register });
      this.toasts.show(register ? "You're registered" : 'Registration cancelled');
      return true;
    } catch (error) {
      this.toasts.show(errorMessage(error), 'error');
      return false;
    } finally {
      this.busy.set(false);
    }
  }

  private focusAfterRender(target: () => ElementRef<HTMLElement> | undefined): void {
    afterNextRender(() => target()?.nativeElement.focus(), { injector: this.injector });
  }
}

import { httpResource } from '@angular/common/http';
import {
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideCircleCheck } from '@lucide/angular';
import { errorMessage } from '../../../core/api/api-errors';
import type { Page } from '../../../core/api/page';
import { REDIRECT_PARAM, SIGN_IN_PATH } from '../../../core/auth/auth-paths';
import { AuthStore } from '../../../core/auth/auth-store';
import {
  CONTEST_QUERY_PARAM,
  CONTESTS_PATH,
  type Contest,
  ContestsApi,
  contestLetter,
  contestSolvedQuery,
  contestSolvedRequest,
  sameContestSolvedQuery,
} from '../../../core/contests/contests-api';
import { PROBLEM_PATH } from '../../../core/problems/problems-api';
import type { Submission } from '../../../core/submissions/submissions';
import { Button } from '../../../ui/button';
import { Countdown } from '../../../ui/countdown';
import { ToastStore } from '../../../ui/toast-store';

type ContestTab = {
  id: string;
  letter: string;
  points: number;
  solved: boolean;
  current: boolean;
};

@Component({
  selector: 'cc-contest-bar',
  imports: [RouterLink, LucideCircleCheck, Button, Countdown],
  host: { class: 'block' },
  template: `
    <div class="flex flex-col gap-3 rounded-panel border border-line bg-raised px-4 py-3 sm:px-5">
      <div class="flex flex-wrap items-center justify-between gap-x-6 gap-y-2">
        <a
          [routerLink]="contestPath()"
          class="min-w-0 truncate rounded-chip font-semibold text-ink underline-offset-4 hover:underline"
          >{{ contest().name }}</a
        >
        <div class="flex items-baseline gap-2">
          <span class="text-sm text-ink-muted">Ends in</span>
          <cc-countdown
            class="text-lg font-bold text-ink"
            [until]="contest().endAt"
            label="Ends in"
          />
        </div>
      </div>

      <div class="flex flex-wrap items-center justify-between gap-3">
        <ul class="flex flex-wrap gap-2" aria-label="Contest problems">
          @for (tab of tabs(); track tab.id) {
            <li>
              <a
                [routerLink]="[problemPath, tab.id]"
                [queryParams]="contestParams()"
                [attr.aria-current]="tab.current ? 'page' : null"
                [attr.aria-label]="tabLabel(tab)"
                class="inline-flex min-h-9 items-center gap-1.5 rounded-chip border px-3 text-sm font-medium tabular-nums transition-colors"
                [class]="
                  tab.current
                    ? 'border-ink bg-ink text-surface'
                    : 'border-control bg-raised text-ink hover:bg-ink/4'
                "
              >
                <span class="font-bold">{{ tab.letter }}</span>
                <span [class.text-ink-muted]="!tab.current">· {{ tab.points }}</span>
                @if (tab.solved) {
                  <svg
                    lucideCircleCheck
                    class="size-4"
                    [class.text-success]="!tab.current"
                    aria-hidden="true"
                  ></svg>
                }
              </a>
            </li>
          }
        </ul>

        @if (!auth.isAuthenticated()) {
          <a ccButton variant="secondary" [routerLink]="signInPath" [queryParams]="signInParams()"
            >Sign in to register</a
          >
        } @else if (contest().registered) {
          <p class="text-sm font-semibold text-success">Registered</p>
        } @else {
          <div class="flex flex-wrap items-center gap-3">
            <p class="text-sm text-ink-muted">Not registered: solves won't count</p>
            <button ccButton type="button" [loading]="busy()" (click)="register()">Register</button>
          </div>
        }
      </div>
    </div>
  `,
})
export class ContestBar {
  private readonly api = inject(ContestsApi);
  private readonly toasts = inject(ToastStore);
  private readonly userId = computed(() => this.auth.session()?.userId ?? null);

  protected readonly auth = inject(AuthStore);
  protected readonly problemPath = PROBLEM_PATH;
  protected readonly signInPath = SIGN_IN_PATH;

  readonly contest = input.required<Contest>();
  readonly questionId = input.required<string>();
  readonly refresh = input(0);
  readonly registered = output();

  protected readonly busy = signal(false);
  protected readonly contestPath = computed(() => [CONTESTS_PATH, this.contest().id]);
  protected readonly contestParams = computed(() => ({
    [CONTEST_QUERY_PARAM]: this.contest().id,
  }));
  protected readonly signInParams = computed(() => ({
    [REDIRECT_PARAM]: `${PROBLEM_PATH}/${this.questionId()}?${CONTEST_QUERY_PARAM}=${encodeURIComponent(this.contest().id)}`,
  }));

  private readonly solvedQuery = computed(() => contestSolvedQuery(this.userId(), this.contest()), {
    equal: sameContestSolvedQuery,
  });
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
  protected readonly tabs = computed<ContestTab[]>(() => {
    const solved = this.solvedIds();
    const current = this.questionId();
    return (this.contest().questions ?? []).map((question, index) => ({
      id: question.id,
      letter: contestLetter(index),
      points: question.points,
      solved: solved.has(question.id),
      current: question.id === current,
    }));
  });

  private staleRefresh = false;

  constructor() {
    let previous: string | null = null;
    effect(() => {
      const trigger = `${this.refresh()}:${this.questionId()}`;
      if (previous !== null && trigger !== previous) {
        untracked(() => {
          this.staleRefresh = !this.solved.reload();
        });
      }
      previous = trigger;
    });
    effect(() => {
      if (!this.solved.isLoading() && this.staleRefresh) {
        untracked(() => {
          this.staleRefresh = !this.solved.reload();
        });
      }
    });
  }

  protected tabLabel(tab: ContestTab): string {
    const status = tab.solved ? ', solved' : '';
    return `Problem ${tab.letter}, ${tab.points} points${status}`;
  }

  protected async register(): Promise<void> {
    if (this.busy()) {
      return;
    }
    this.busy.set(true);
    try {
      await this.api.register(this.contest().id);
      this.toasts.show("You're registered");
      this.registered.emit();
    } catch (error) {
      this.toasts.show(errorMessage(error), 'error');
    } finally {
      this.busy.set(false);
    }
  }
}

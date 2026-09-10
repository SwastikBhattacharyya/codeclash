import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  computed,
  DestroyRef,
  DOCUMENT,
  effect,
  inject,
  input,
  linkedSignal,
  output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { Router } from '@angular/router';
import { LucideRotateCcw, LucideSend } from '@lucide/angular';
import { errorMessage } from '../../core/api/api-errors';
import { REDIRECT_PARAM, SIGN_IN_PATH } from '../../core/auth/auth-paths';
import { AuthStore } from '../../core/auth/auth-store';
import { LocalStore } from '../../core/storage/local-store';
import {
  isSubmissionLanguage,
  LANGUAGE_LABELS,
  LANGUAGE_TIME_MULTIPLIERS,
  SUBMISSION_CODE_MAX_SIZE,
  SUBMISSION_LANGUAGES,
  type Submission,
  type SubmissionLanguage,
  SubmissionsApi,
} from '../../core/submissions/submissions';
import { Alert } from '../../ui/alert';
import { Button } from '../../ui/button';
import { CodeEditor } from '../../ui/code-editor';
import { Select, type SelectOption } from '../../ui/select';
import { SOLVE_TEMPLATES } from './solve-templates';

const LANGUAGE_KEY = 'codeclash:solve:language';
const DEFAULT_LANGUAGE: SubmissionLanguage = 'cpp';
const DRAFT_SAVE_MS = 400;
const MILLISECONDS_PER_SECOND = 1000;
const FORBIDDEN_STATUS = 403;
const UNAUTHORIZED_STATUS = 401;
const RESET_MESSAGE = 'Replace your code with the starting template?';
const LOAD_MESSAGE = 'Replace your current code with this submission?';
const NON_SPACE = /\S/;
const HIGH_SURROGATE_MIN = 0xd800;
const HIGH_SURROGATE_MAX = 0xdbff;
const LOW_SURROGATE_MIN = 0xdc00;
const LOW_SURROGATE_MAX = 0xdfff;
const LANGUAGE_OPTIONS: readonly SelectOption[] = SUBMISSION_LANGUAGES.map((language) => ({
  value: language,
  label: LANGUAGE_LABELS[language],
}));

type DraftTarget = {
  key: string;
  language: SubmissionLanguage;
};

type Draft = DraftTarget & {
  code: string;
};

@Component({
  selector: 'cc-solve-panel',
  imports: [LucideRotateCcw, LucideSend, Alert, Button, CodeEditor, Select],
  host: { class: 'flex flex-col gap-3' },
  templateUrl: './solve-panel.html',
})
export class SolvePanel {
  private readonly store = inject(LocalStore);
  private readonly api = inject(SubmissionsApi);
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly document = inject(DOCUMENT);
  private readonly editor = viewChild.required(CodeEditor);

  readonly questionId = input.required<string>();
  readonly timeLimitMs = input.required<number>();
  readonly draftKeyPrefix = input('codeclash:draft');
  readonly blockedReason = input<string | null>(null);
  readonly judging = input(false);
  readonly submitted = output<Submission>();

  protected readonly languageOptions = LANGUAGE_OPTIONS;
  protected readonly maxSize = SUBMISSION_CODE_MAX_SIZE;
  protected readonly signedIn = this.auth.isAuthenticated;
  protected readonly language = signal<SubmissionLanguage>(this.storedLanguage());
  private readonly draftTarget = computed<DraftTarget>(
    () => ({ key: this.keyFor(this.language()), language: this.language() }),
    { equal: (left, right) => left.key === right.key },
  );
  protected readonly code = linkedSignal<DraftTarget, string>({
    source: this.draftTarget,
    computation: (target) => this.store.get(target.key) ?? SOLVE_TEMPLATES[target.language],
  });
  protected readonly size = computed(() => SolvePanel.codePoints(this.code()));
  protected readonly tooLong = computed(() => this.size() > SUBMISSION_CODE_MAX_SIZE);
  protected readonly blank = computed(() => !NON_SPACE.test(this.code()));
  protected readonly limitHint = computed(() => {
    const language = this.language();
    const seconds =
      (this.timeLimitMs() * LANGUAGE_TIME_MULTIPLIERS[language]) / MILLISECONDS_PER_SECOND;
    return `${LANGUAGE_LABELS[language]}: ${seconds.toLocaleString('en-US', { maximumFractionDigits: 2 })} s per test`;
  });
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly canSubmit = computed(
    () =>
      !this.submitting() &&
      !this.judging() &&
      !this.tooLong() &&
      !this.blank() &&
      this.blockedReason() === null,
  );

  private pendingDraft: Draft | null = null;

  constructor() {
    effect((onCleanup) => {
      const draft: Draft = { ...this.draftTarget(), code: this.code() };
      const previous = this.pendingDraft;
      if (previous !== null && previous.key !== draft.key) {
        untracked(() => this.saveDraft(previous));
      }
      this.pendingDraft = draft;
      const handle = setTimeout(() => untracked(() => this.saveDraft(draft)), DRAFT_SAVE_MS);
      onCleanup(() => clearTimeout(handle));
    });
    inject(DestroyRef).onDestroy(() => {
      if (this.pendingDraft !== null) {
        this.saveDraft(this.pendingDraft);
      }
    });
  }

  load(code: string, language: SubmissionLanguage): void {
    this.flushDraft();
    const existing = this.store.get(this.keyFor(language));
    if (
      existing !== null &&
      existing !== code &&
      !(this.document.defaultView?.confirm(LOAD_MESSAGE) ?? true)
    ) {
      return;
    }
    this.selectLanguage(language);
    this.code.set(code);
    this.editor().focus();
  }

  protected onLanguage(value: string): void {
    if (isSubmissionLanguage(value)) {
      this.flushDraft();
      this.selectLanguage(value);
    }
  }

  protected reset(): void {
    const template = SOLVE_TEMPLATES[this.language()];
    if (this.code() === template) {
      return;
    }
    if (this.document.defaultView?.confirm(RESET_MESSAGE) ?? true) {
      this.code.set(template);
    }
  }

  protected async submit(): Promise<void> {
    if (!this.signedIn()) {
      await this.signIn();
      return;
    }
    if (!this.canSubmit()) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    try {
      const submission = await this.api.create({
        questionId: this.questionId(),
        language: this.language(),
        code: this.code(),
      });
      this.flushDraft();
      this.submitted.emit(submission);
    } catch (error) {
      this.error.set(SolvePanel.submitError(error));
    } finally {
      this.submitting.set(false);
    }
  }

  protected async signIn(): Promise<void> {
    this.flushDraft();
    await this.router.navigate([SIGN_IN_PATH], {
      queryParams: { [REDIRECT_PARAM]: this.router.url },
    });
  }

  private selectLanguage(language: SubmissionLanguage): void {
    this.language.set(language);
    this.store.set(LANGUAGE_KEY, language);
  }

  private keyFor(language: SubmissionLanguage): string {
    return `${this.draftKeyPrefix()}:${this.questionId()}:${language}`;
  }

  private flushDraft(): void {
    this.saveDraft({ ...untracked(this.draftTarget), code: untracked(this.code) });
  }

  private saveDraft(draft: Draft): void {
    if (this.pendingDraft?.key === draft.key) {
      this.pendingDraft = null;
    }
    if (draft.code === SOLVE_TEMPLATES[draft.language]) {
      this.store.remove(draft.key);
    } else {
      this.store.set(draft.key, draft.code);
    }
  }

  private storedLanguage(): SubmissionLanguage {
    const stored = this.store.get(LANGUAGE_KEY);
    return isSubmissionLanguage(stored) ? stored : DEFAULT_LANGUAGE;
  }

  private static codePoints(text: string): number {
    let pairs = 0;
    for (let index = 0; index < text.length; index++) {
      const unit = text.charCodeAt(index);
      if (unit < HIGH_SURROGATE_MIN || unit > HIGH_SURROGATE_MAX) {
        continue;
      }
      const next = text.charCodeAt(index + 1);
      if (next >= LOW_SURROGATE_MIN && next <= LOW_SURROGATE_MAX) {
        pairs++;
        index++;
      }
    }
    return text.length - pairs;
  }

  private static submitError(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === UNAUTHORIZED_STATUS) {
      return 'Your session has ended. Sign in again to submit.';
    }
    if (error instanceof HttpErrorResponse && error.status === FORBIDDEN_STATUS) {
      return "You can't submit to this problem right now.";
    }
    return errorMessage(error);
  }
}

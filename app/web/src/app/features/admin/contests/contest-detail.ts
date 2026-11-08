import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import {
  Component,
  computed,
  DOCUMENT,
  effect,
  Injector,
  inject,
  linkedSignal,
  signal,
  untracked,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  applyEach,
  disabled,
  type FieldTree,
  FormField,
  FormRoot,
  form,
  hidden,
} from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucideEye, LucidePlus, LucideTrash, LucideX } from '@lucide/angular';
import { CONTEST_READ_QUESTIONS } from '../../../core/admin/admin-access';
import { adminSection } from '../../../core/admin/admin-sections';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import { LOOKUP_PAGE_SIZE, type Page } from '../../../core/api/page';
import {
  CONTESTS_PATH,
  type Contest,
  type ContestSummary,
  contestUrl,
} from '../../../core/contests/contests-api';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { type Question, questionsByIdsRequest } from '../../../core/problems/problems-api';
import { Clock, describeDuration } from '../../../core/time/clock';
import { fromLocalInput, toLocalInput } from '../../../core/time/local-datetime';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { DateTimeField } from '../../../ui/datetime-field';
import { MarkdownEditor } from '../../../ui/markdown-editor';
import { Spinner } from '../../../ui/spinner';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { ForbiddenAlert } from '../forbidden-alert';
import { ContestDeleteDialog } from './contest-delete-dialog';
import { ContestQuestionPicker, type PickedQuestion } from './contest-question-picker';
import {
  CONTEST_DESCRIPTION_MAX_SIZE,
  CONTEST_POINTS_DEFAULT,
  CONTEST_POINTS_MAX,
  CONTEST_POINTS_MIN,
  contestDescriptionRules,
  contestNameRules,
  contestPointsRules,
  contestScheduleRules,
} from './contest-rules';
import { ContestStatusBadge } from './contest-status-badge';
import {
  AdminContestsApi,
  CONTEST_ALREADY_LIVE,
  CONTEST_FORBIDDEN_MESSAGE,
  type ContestChanges,
  type ContestQuestionInput,
  contestStarted,
} from './contests-api';

type QuestionRow = {
  id: string;
  points: string;
};

type ContestModel = {
  name: string;
  description: string;
  liveAt: string;
  endAt: string;
  questions: QuestionRow[];
};

const EMPTY_MODEL: ContestModel = {
  name: '',
  description: '',
  liveAt: '',
  endAt: '',
  questions: [],
};
const QUESTIONS_NOT_FOUND = 'QUESTIONS_NOT_FOUND';
const DUPLICATE_QUESTIONS = 'DUPLICATE_QUESTIONS';
const DISCARD_MESSAGE = 'Discard unsaved changes?';
const ALREADY_LIVE_MESSAGE =
  'This contest has started, so its start time and questions can no longer change. They have been reset; your other changes are kept, so save again.';
const STARTED_WHILE_EDITING_MESSAGE =
  'This contest started while you were editing, so your changes to its start time and questions were undone.';
const SUBMIT_OVERRIDES: Readonly<Record<string, string>> = {
  FORBIDDEN: CONTEST_FORBIDDEN_MESSAGE,
  [QUESTIONS_NOT_FOUND]:
    "The highlighted questions don't exist anymore, or haven't reached the contest service yet. Remove them or try again shortly.",
  [DUPLICATE_QUESTIONS]: 'The highlighted questions are listed more than once.',
};
const DESCRIPTION_HINT = 'Markdown and $math$ / $$math$$ (KaTeX) are supported.';
const UNAVAILABLE_TITLE = 'Unavailable question';
const LOADING_TITLE = 'Loading title…';
const FAILED_TITLE = "Title couldn't be loaded";
const DIGITS_PATTERN = /^\d+$/;

@Component({
  selector: 'cc-contest-detail',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    RouterLink,
    LucideArrowLeft,
    LucideEye,
    LucidePlus,
    LucideTrash,
    LucideX,
    Alert,
    Button,
    ContestDeleteDialog,
    ContestQuestionPicker,
    ContestStatusBadge,
    DateTimeField,
    ForbiddenAlert,
    MarkdownEditor,
    Spinner,
    TextField,
  ],
  host: {
    class: 'flex flex-col gap-6 sm:gap-8',
    '(window:beforeunload)': 'onBeforeUnload($event)',
  },
  templateUrl: './contest-detail.html',
})
export class ContestDetail {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly document = inject(DOCUMENT);
  private readonly injector = inject(Injector);
  private readonly toasts = inject(ToastStore);
  private readonly api = inject(AdminContestsApi);
  private readonly clock = inject(Clock);
  private readonly params = toSignal(this.route.paramMap, { requireSync: true });
  private leaving = false;

  protected readonly section = adminSection('contests');
  protected readonly contestsPath = CONTESTS_PATH;
  protected readonly descriptionMaxSize = CONTEST_DESCRIPTION_MAX_SIZE;
  protected readonly descriptionHint = DESCRIPTION_HINT;
  protected readonly pointsHint = `${CONTEST_POINTS_MIN}–${CONTEST_POINTS_MAX}`;
  protected readonly readQuestionsPermission = [CONTEST_READ_QUESTIONS];

  protected readonly contestId = computed(() => this.params().get('id'));
  protected readonly creating = computed(() => this.contestId() === null);
  protected readonly contest = httpResource<Contest>(() => {
    const id = this.contestId();
    return id ? contestUrl(id) : undefined;
  });
  protected readonly loadedContest = computed(() =>
    !this.creating() && this.contest.hasValue() ? this.contest.value() : null,
  );
  protected readonly ready = computed(() => this.creating() || this.loadedContest() !== null);
  protected readonly title = computed(() =>
    this.creating() ? 'New contest' : (this.loadedContest()?.name ?? 'Contest'),
  );
  protected readonly locked = computed(() => {
    const contest = this.loadedContest();
    return contest !== null && contestStarted(contest, this.clock.now());
  });
  protected readonly questionsVisible = computed(
    () => this.creating() || this.loadedContest()?.questions != null,
  );
  protected readonly questionsEditable = computed(() => this.questionsVisible() && !this.locked());

  private readonly baseline = computed<ContestModel>(() => {
    const contest = this.loadedContest();
    if (contest === null) {
      return EMPTY_MODEL;
    }
    return {
      name: contest.name,
      description: contest.description,
      liveAt: toLocalInput(contest.liveAt),
      endAt: toLocalInput(contest.endAt),
      questions: (contest.questions ?? []).map((question) => ({
        id: question.id,
        points: String(question.points),
      })),
    };
  });
  private readonly model = linkedSignal<ContestModel>(() => ContestDetail.copy(this.baseline()));
  protected readonly dirty = computed(() => {
    const baseline = this.baseline();
    const model = this.model();
    return (
      model.name.trim() !== baseline.name ||
      model.description.trim() !== baseline.description ||
      (!this.locked() && model.liveAt !== baseline.liveAt) ||
      model.endAt !== baseline.endAt ||
      (this.questionsEditable() && !ContestDetail.sameRows(model.questions, baseline.questions))
    );
  });

  private readonly questionIds = computed(
    () =>
      this.model()
        .questions.map((question) => question.id)
        .sort(),
    { equal: (left, right) => left.join() === right.join() },
  );
  private readonly knownTitles = signal<ReadonlyMap<string, string>>(new Map());
  private readonly unavailableIds = signal<ReadonlySet<string>>(new Set());
  private readonly missingIds = computed(
    () => {
      const known = this.knownTitles();
      const unavailable = this.unavailableIds();
      return this.questionIds()
        .filter((id) => !known.has(id) && !unavailable.has(id))
        .slice(0, LOOKUP_PAGE_SIZE);
    },
    { equal: (left, right) => left.join() === right.join() },
  );
  private readonly questionLookup = httpResource<Page<Question>>(() => {
    const ids = this.missingIds();
    return ids.length > 0 ? questionsByIdsRequest(ids) : undefined;
  });
  protected readonly flaggedIds = signal<ReadonlySet<string>>(new Set());
  protected readonly addedIds = computed(() => new Set(this.questionIds()));
  protected readonly duration = computed(() => {
    const { liveAt, endAt } = this.model();
    const start = fromLocalInput(liveAt);
    const end = fromLocalInput(endAt);
    if (start === null || end === null) {
      return null;
    }
    const length = Date.parse(end) - Date.parse(start);
    return length > 0 ? describeDuration(length) : null;
  });

  protected readonly formError = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);
  protected readonly deleteOpen = signal(false);
  protected readonly pickerOpen = signal(false);

  protected readonly contestForm = form(
    this.model,
    (path) => {
      contestNameRules(path.name);
      contestDescriptionRules(path.description);
      contestScheduleRules(path.liveAt, path.endAt, {
        now: () => this.clock.now(),
        changed: (field) => this.creating() || this.model()[field] !== this.baseline()[field],
      });
      applyEach(path.questions, (row) => contestPointsRules(row.points));
      disabled(path.liveAt, { when: () => this.locked() });
      disabled(path.questions, { when: () => this.locked() });
      hidden(path.questions, { when: () => !this.questionsVisible() });
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          this.missing.set([]);
          this.flaggedIds.set(new Set());
          const model = field().value();
          const current = this.loadedContest();
          try {
            if (current === null) {
              const created = await this.api.create({
                name: model.name.trim(),
                description: model.description.trim(),
                liveAt: ContestDetail.instant(model.liveAt),
                endAt: ContestDetail.instant(model.endAt),
                questions: ContestDetail.questionInputs(model.questions),
              });
              this.toasts.show(`Contest “${created.name}” created`);
              await this.leave([this.section.path, created.id]);
              return undefined;
            }
            const changes = this.changesFrom(current, model);
            if (Object.keys(changes).length === 0) {
              return undefined;
            }
            const updated = await this.api.update(current.id, changes);
            this.contest.set(updated);
            this.formError.set(null);
            this.toasts.show(`Contest “${updated.name}” saved`);
            return undefined;
          } catch (error) {
            if (current !== null && isGone(error)) {
              await this.onGone();
              return undefined;
            }
            const apiError = toApiError(error);
            if (current !== null && apiError?.code === CONTEST_ALREADY_LIVE) {
              await this.reloadKeepingEdits(current.id);
              return undefined;
            }
            if (apiError?.code === QUESTIONS_NOT_FOUND || apiError?.code === DUPLICATE_QUESTIONS) {
              this.flaggedIds.set(ContestDetail.idsFrom(apiError.properties?.['ids']));
            }
            const fields = this.fieldList(field);
            const failure = toSubmitFailure(
              error,
              {
                name: field.name,
                description: field.description,
                liveAt: field.liveAt,
                endAt: field.endAt,
              },
              SUBMIT_OVERRIDES,
            );
            this.formError.set(failure.formError);
            this.missing.set(missingPermissions(error));
            if (failure.fieldErrors.length > 0) {
              focusFirstInvalidAfterRender(this.injector, fields);
            }
            return failure.fieldErrors;
          }
        },
        onInvalid: (field) => focusFirstInvalid(this.fieldList(field)),
      },
    },
  );

  protected readonly loadError = computed(() => {
    const error = this.contest.error();
    return error && !isGone(error) ? errorMessage(error) : null;
  });

  constructor() {
    effect(() => {
      const error = this.contest.error();
      if (error && !this.creating() && isGone(error)) {
        untracked(() => void this.onGone());
      }
    });
    effect(() => {
      if (!this.questionLookup.hasValue()) {
        return;
      }
      const page = this.questionLookup.value();
      untracked(() => this.rememberTitles(this.missingIds(), page.data, page.hasNext));
    });
    effect(() => {
      if (this.locked()) {
        untracked(() => this.dropLockedEdits());
      }
    });
  }

  canLeave(): boolean {
    if (!this.hasUnsavedChanges()) {
      return true;
    }
    return this.document.defaultView?.confirm(DISCARD_MESSAGE) ?? true;
  }

  protected onBeforeUnload(event: BeforeUnloadEvent): void {
    if (this.hasUnsavedChanges()) {
      event.preventDefault();
    }
  }

  protected questionTitle(id: string): string {
    const title = this.knownTitles().get(id);
    if (title !== undefined) {
      return title;
    }
    if (this.unavailableIds().has(id)) {
      return UNAVAILABLE_TITLE;
    }
    return this.questionLookup.error() ? FAILED_TITLE : LOADING_TITLE;
  }

  protected isFlagged(id: string): boolean {
    return this.flaggedIds().has(id);
  }

  protected removeQuestion(id: string): void {
    if (!this.questionsEditable()) {
      return;
    }
    this.model.update((model) => ({
      ...model,
      questions: model.questions.filter((question) => question.id !== id),
    }));
    this.flaggedIds.update((flagged) => {
      const next = new Set(flagged);
      next.delete(id);
      return next;
    });
  }

  protected onPicked(picked: readonly PickedQuestion[]): void {
    this.pickerOpen.set(false);
    if (!this.questionsEditable()) {
      return;
    }
    this.knownTitles.update((titles) => {
      const next = new Map(titles);
      for (const question of picked) {
        next.set(question.id, question.title);
      }
      return next;
    });
    this.model.update((model) => {
      const existing = new Set(model.questions.map((question) => question.id));
      const additions = picked
        .filter((question) => !existing.has(question.id))
        .map((question) => ({ id: question.id, points: String(CONTEST_POINTS_DEFAULT) }));
      return { ...model, questions: [...model.questions, ...additions] };
    });
  }

  protected discard(): void {
    this.model.set(ContestDetail.copy(this.baseline()));
    this.formError.set(null);
    this.missing.set([]);
    this.flaggedIds.set(new Set());
    this.contestForm().reset();
  }

  protected async onDeleted(contest: ContestSummary): Promise<void> {
    this.deleteOpen.set(false);
    this.toasts.show(`Contest “${contest.name}” deleted`);
    await this.leave([this.section.path]);
  }

  protected async onGone(): Promise<void> {
    this.deleteOpen.set(false);
    this.toasts.show('That contest no longer exists', 'error');
    await this.leave([this.section.path]);
  }

  private changesFrom(contest: Contest, model: ContestModel): ContestChanges {
    const baseline = this.baseline();
    const changes: ContestChanges = {};
    const name = model.name.trim();
    if (name !== contest.name) {
      changes.name = name;
    }
    const description = model.description.trim();
    if (description !== contest.description) {
      changes.description = description;
    }
    if (!this.locked() && model.liveAt !== baseline.liveAt) {
      changes.liveAt = ContestDetail.instant(model.liveAt);
    }
    if (model.endAt !== baseline.endAt) {
      changes.endAt = ContestDetail.instant(model.endAt);
    }
    if (this.questionsEditable() && !ContestDetail.sameRows(model.questions, baseline.questions)) {
      changes.questions = ContestDetail.questionInputs(model.questions);
    }
    return changes;
  }

  private async reloadKeepingEdits(id: string): Promise<void> {
    try {
      const fresh = await this.api.get(id);
      const model = untracked(this.model);
      this.contest.set(fresh);
      this.model.update((current) => ({
        ...current,
        name: model.name,
        description: model.description,
        endAt: model.endAt,
      }));
      this.formError.set(ALREADY_LIVE_MESSAGE);
    } catch (error) {
      if (isGone(error)) {
        await this.onGone();
        return;
      }
      this.formError.set(errorMessage(error));
    }
  }

  private dropLockedEdits(): void {
    this.pickerOpen.set(false);
    const baseline = this.baseline();
    const model = this.model();
    if (
      model.liveAt === baseline.liveAt &&
      ContestDetail.sameRows(model.questions, baseline.questions)
    ) {
      return;
    }
    this.model.update((current) => ({
      ...current,
      liveAt: baseline.liveAt,
      questions: ContestDetail.copy(baseline).questions,
    }));
    this.formError.set(STARTED_WHILE_EDITING_MESSAGE);
  }

  private rememberTitles(
    requested: readonly string[],
    found: readonly Question[],
    truncated: boolean,
  ): void {
    const foundIds = new Set(found.map((question) => question.id));
    this.knownTitles.update((titles) => {
      const next = new Map(titles);
      for (const question of found) {
        next.set(question.id, question.title);
      }
      return next;
    });
    const missing = requested.filter((id) => !foundIds.has(id));
    if (!truncated && missing.length > 0) {
      this.unavailableIds.update((ids) => new Set([...ids, ...missing]));
    }
  }

  private hasUnsavedChanges(): boolean {
    return !this.leaving && this.dirty();
  }

  private async leave(commands: readonly string[]): Promise<void> {
    this.leaving = true;
    try {
      this.leaving = await this.router.navigate(commands);
    } catch {
      this.leaving = false;
    }
  }

  private fieldList(field: FieldTree<ContestModel>): FieldTree<unknown>[] {
    return [
      field.name,
      field.description,
      field.liveAt,
      field.endAt,
      ...[...field.questions].map((row) => row.points),
    ];
  }

  private static copy(model: ContestModel): ContestModel {
    return { ...model, questions: model.questions.map((question) => ({ ...question })) };
  }

  private static instant(value: string): string {
    return fromLocalInput(value) ?? value;
  }

  private static questionInputs(rows: readonly QuestionRow[]): ContestQuestionInput[] {
    return rows.map((row) => ({ id: row.id, points: Number(row.points.trim()) }));
  }

  private static sameRows(left: readonly QuestionRow[], right: readonly QuestionRow[]): boolean {
    if (left.length !== right.length) {
      return false;
    }
    const points = new Map(right.map((row) => [row.id, ContestDetail.pointsText(row.points)]));
    return left.every((row) => points.get(row.id) === ContestDetail.pointsText(row.points));
  }

  private static pointsText(value: string): string {
    const text = value.trim();
    return DIGITS_PATTERN.test(text) ? String(Number(text)) : text;
  }

  private static idsFrom(value: unknown): ReadonlySet<string> {
    return new Set(
      Array.isArray(value) ? value.filter((id): id is string => typeof id === 'string') : [],
    );
  }
}

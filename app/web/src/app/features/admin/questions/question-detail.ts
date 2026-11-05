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
import { type FieldTree, FormField, FormRoot, form } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucideEye, LucideListChecks, LucideTrash } from '@lucide/angular';
import { SUBMISSION_DELETE } from '../../../core/admin/admin-access';
import { adminSection } from '../../../core/admin/admin-sections';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import type { Page } from '../../../core/api/page';
import { AuthStore } from '../../../core/auth/auth-store';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import {
  CATEGORIES_URL,
  type Category,
  lookupRequest,
  PROBLEM_PATH,
  questionUrl,
  TAGS_URL,
  type Tag,
} from '../../../core/problems/problems-api';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Checkbox } from '../../../ui/checkbox';
import { type ChipOption, ChipPicker, type ChipPickerStatus } from '../../../ui/chip-picker';
import { MarkdownEditor } from '../../../ui/markdown-editor';
import { Spinner } from '../../../ui/spinner';
import { TextArea } from '../../../ui/text-area';
import { TextField } from '../../../ui/text-field';
import { ToastStore } from '../../../ui/toast-store';
import { ForbiddenAlert } from '../forbidden-alert';
import { QuestionDeleteDialog } from './question-delete-dialog';
import {
  QUESTION_DESCRIPTION_MAX_SIZE,
  QUESTION_RAM_DEFAULT_MB,
  QUESTION_RAM_MAX_MB,
  QUESTION_RAM_MIN_MB,
  QUESTION_RUN_TIME_DEFAULT_MS,
  QUESTION_RUN_TIME_MAX_MS,
  QUESTION_RUN_TIME_MIN_MS,
  QUESTION_SUMMARY_MAX_SIZE,
  questionDescriptionRules,
  questionRamRules,
  questionRunTimeRules,
  questionSummaryRules,
  questionTitleRules,
} from './question-rules';
import {
  type AdminQuestion,
  QUESTION_FORBIDDEN_MESSAGE,
  type QuestionChanges,
  type QuestionInput,
  QuestionsApi,
} from './questions-api';
import { TestCasesSection } from './test-cases-section';

type QuestionModel = {
  title: string;
  summary: string;
  description: string;
  maxRunTimeMs: string;
  maxRamLimitMb: string;
  isVisible: boolean;
  categories: string[];
  tags: string[];
};

const EMPTY_MODEL: QuestionModel = {
  title: '',
  summary: '',
  description: '',
  maxRunTimeMs: String(QUESTION_RUN_TIME_DEFAULT_MS),
  maxRamLimitMb: String(QUESTION_RAM_DEFAULT_MB),
  isVisible: false,
  categories: [],
  tags: [],
};
const CATEGORIES_NOT_FOUND = 'CATEGORIES_NOT_FOUND';
const TAGS_NOT_FOUND = 'TAGS_NOT_FOUND';
const DISCARD_MESSAGE = 'Discard unsaved changes?';
const SUBMIT_OVERRIDES: Readonly<Record<string, string>> = {
  FORBIDDEN: QUESTION_FORBIDDEN_MESSAGE,
  [CATEGORIES_NOT_FOUND]: 'Some categories no longer exist. The list has been refreshed.',
  [TAGS_NOT_FOUND]: 'Some tags no longer exist. The list has been refreshed.',
  QUESTION_IN_RUNNING_CONTEST:
    "It's in a running contest, so it can't be made private until the contest ends.",
};
const DESCRIPTION_HINT = 'Markdown and $math$ / $$math$$ (KaTeX) are supported.';

@Component({
  selector: 'cc-question-detail',
  imports: [
    DatePipe,
    FormRoot,
    FormField,
    RouterLink,
    LucideArrowLeft,
    LucideEye,
    LucideListChecks,
    LucideTrash,
    Alert,
    Button,
    Checkbox,
    ChipPicker,
    ForbiddenAlert,
    MarkdownEditor,
    QuestionDeleteDialog,
    Spinner,
    TestCasesSection,
    TextArea,
    TextField,
  ],
  host: {
    class: 'flex flex-col gap-6 sm:gap-8',
    '(window:beforeunload)': 'onBeforeUnload($event)',
  },
  templateUrl: './question-detail.html',
})
export class QuestionDetail {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly document = inject(DOCUMENT);
  private readonly injector = inject(Injector);
  private readonly toasts = inject(ToastStore);
  private readonly api = inject(QuestionsApi);
  private readonly auth = inject(AuthStore);
  private readonly params = toSignal(this.route.paramMap, { requireSync: true });
  private leaving = false;

  protected readonly section = adminSection('questions');
  protected readonly problemPath = PROBLEM_PATH;
  protected readonly submissionsPath = adminSection('submissions').path;
  protected readonly canViewSubmissions = computed(
    () => this.auth.session()?.permissions.includes(SUBMISSION_DELETE) ?? false,
  );
  protected readonly summaryMaxSize = QUESTION_SUMMARY_MAX_SIZE;
  protected readonly descriptionMaxSize = QUESTION_DESCRIPTION_MAX_SIZE;
  protected readonly descriptionHint = DESCRIPTION_HINT;
  protected readonly runTimeHint = `${QUESTION_RUN_TIME_MIN_MS}–${QUESTION_RUN_TIME_MAX_MS} ms`;
  protected readonly ramHint = `${QUESTION_RAM_MIN_MB}–${QUESTION_RAM_MAX_MB} MB`;

  protected readonly questionId = computed(() => this.params().get('id'));
  protected readonly creating = computed(() => this.questionId() === null);
  protected readonly question = httpResource<AdminQuestion>(() => {
    const id = this.questionId();
    return id ? questionUrl(id) : undefined;
  });
  protected readonly loadedQuestion = computed(() =>
    !this.creating() && this.question.hasValue() ? this.question.value() : null,
  );
  protected readonly ready = computed(() => this.creating() || this.loadedQuestion() !== null);
  protected readonly title = computed(() =>
    this.creating() ? 'New question' : (this.loadedQuestion()?.title ?? 'Question'),
  );

  protected readonly categories = httpResource<Page<Category>>(() => lookupRequest(CATEGORIES_URL));
  protected readonly tags = httpResource<Page<Tag>>(() => lookupRequest(TAGS_URL));
  protected readonly categoryOptions = computed<readonly ChipOption[]>(() =>
    this.categories.hasValue() ? this.categories.value().data : [],
  );
  protected readonly tagOptions = computed<readonly ChipOption[]>(() =>
    this.tags.hasValue() ? this.tags.value().data : [],
  );
  protected readonly categoryStatus = computed(() => QuestionDetail.lookupStatus(this.categories));
  protected readonly tagStatus = computed(() => QuestionDetail.lookupStatus(this.tags));

  private readonly baseline = computed<QuestionModel>(() => {
    const question = this.loadedQuestion();
    if (question === null) {
      return EMPTY_MODEL;
    }
    return {
      title: question.title,
      summary: question.summary,
      description: question.description,
      maxRunTimeMs: String(question.maxRunTimeMs),
      maxRamLimitMb: String(question.maxRamLimitMb),
      isVisible: question.isVisible,
      categories: question.categories,
      tags: question.tags,
    };
  });
  private readonly model = linkedSignal<QuestionModel>(() => ({ ...this.baseline() }));
  protected readonly dirty = computed(() => {
    const baseline = this.baseline();
    const model = this.model();
    return (
      model.title.trim() !== baseline.title ||
      model.summary.trim() !== baseline.summary ||
      model.description.trim() !== baseline.description ||
      QuestionDetail.numberText(model.maxRunTimeMs) !== baseline.maxRunTimeMs ||
      QuestionDetail.numberText(model.maxRamLimitMb) !== baseline.maxRamLimitMb ||
      model.isVisible !== baseline.isVisible ||
      !QuestionDetail.sameIds(model.categories, baseline.categories) ||
      !QuestionDetail.sameIds(model.tags, baseline.tags)
    );
  });

  protected readonly formError = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);
  protected readonly deleteOpen = signal(false);

  protected readonly questionForm = form(
    this.model,
    (path) => {
      questionTitleRules(path.title);
      questionSummaryRules(path.summary);
      questionDescriptionRules(path.description);
      questionRunTimeRules(path.maxRunTimeMs);
      questionRamRules(path.maxRamLimitMb);
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          this.missing.set([]);
          const input = QuestionDetail.inputFrom(field().value());
          const current = this.loadedQuestion();
          try {
            if (current === null) {
              const created = await this.api.create(input);
              this.toasts.show(`Question “${created.title}” created`);
              await this.leave([this.section.path, created.id]);
              return undefined;
            }
            const changes = QuestionDetail.changesFrom(current, input);
            if (Object.keys(changes).length === 0) {
              return undefined;
            }
            const updated = await this.api.update(current.id, changes);
            this.question.set(updated);
            this.toasts.show(`Question “${updated.title}” saved`);
            return undefined;
          } catch (error) {
            if (current !== null && isGone(error)) {
              await this.onGone();
              return undefined;
            }
            const code = toApiError(error)?.code;
            if (code === CATEGORIES_NOT_FOUND) {
              this.categories.reload();
            }
            if (code === TAGS_NOT_FOUND) {
              this.tags.reload();
            }
            const fields = this.fieldList(field);
            const failure = toSubmitFailure(
              error,
              {
                title: field.title,
                summary: field.summary,
                description: field.description,
                maxRunTimeMs: field.maxRunTimeMs,
                maxRamLimitMb: field.maxRamLimitMb,
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
    const error = this.question.error();
    return error && !isGone(error) ? errorMessage(error) : null;
  });

  constructor() {
    effect(() => {
      const error = this.question.error();
      if (error && !this.creating() && isGone(error)) {
        untracked(() => void this.onGone());
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

  protected discard(): void {
    this.model.set({ ...this.baseline() });
    this.formError.set(null);
    this.missing.set([]);
    this.questionForm().reset();
  }

  protected async onDeleted(question: AdminQuestion): Promise<void> {
    this.deleteOpen.set(false);
    this.toasts.show(`Question “${question.title}” deleted`);
    await this.leave([this.section.path]);
  }

  protected async onGone(): Promise<void> {
    this.deleteOpen.set(false);
    this.toasts.show('That question no longer exists', 'error');
    await this.leave([this.section.path]);
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

  private fieldList(field: FieldTree<QuestionModel>): FieldTree<unknown>[] {
    return [field.title, field.summary, field.description, field.maxRunTimeMs, field.maxRamLimitMb];
  }

  private static inputFrom(model: QuestionModel): QuestionInput {
    return {
      title: model.title.trim(),
      summary: model.summary.trim(),
      description: model.description.trim(),
      isVisible: model.isVisible,
      maxRunTimeMs: Number(model.maxRunTimeMs.trim()),
      maxRamLimitMb: Number(model.maxRamLimitMb.trim()),
      categories: [...model.categories],
      tags: [...model.tags],
    };
  }

  private static changesFrom(question: AdminQuestion, input: QuestionInput): QuestionChanges {
    const changes: QuestionChanges = {};
    if (input.title !== question.title) {
      changes.title = input.title;
    }
    if (input.summary !== question.summary) {
      changes.summary = input.summary;
    }
    if (input.description !== question.description) {
      changes.description = input.description;
    }
    if (input.isVisible !== question.isVisible) {
      changes.isVisible = input.isVisible;
    }
    if (input.maxRunTimeMs !== question.maxRunTimeMs) {
      changes.maxRunTimeMs = input.maxRunTimeMs;
    }
    if (input.maxRamLimitMb !== question.maxRamLimitMb) {
      changes.maxRamLimitMb = input.maxRamLimitMb;
    }
    if (!QuestionDetail.sameIds(input.categories, question.categories)) {
      changes.categories = input.categories;
    }
    if (!QuestionDetail.sameIds(input.tags, question.tags)) {
      changes.tags = input.tags;
    }
    return changes;
  }

  private static lookupStatus(resource: {
    hasValue(): boolean;
    error(): unknown;
  }): ChipPickerStatus {
    if (resource.hasValue()) {
      return 'ready';
    }
    return resource.error() ? 'error' : 'loading';
  }

  private static numberText(value: string): string {
    const text = value.trim();
    return /^\d+$/.test(text) ? String(Number(text)) : text;
  }

  private static sameIds(left: readonly string[], right: readonly string[]): boolean {
    if (left.length !== right.length) {
      return false;
    }
    const ids = new Set(right);
    return left.every((id) => ids.has(id));
  }
}

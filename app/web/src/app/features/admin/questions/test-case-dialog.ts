import {
  Component,
  computed,
  type ElementRef,
  Injector,
  inject,
  input,
  linkedSignal,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { type FieldTree, FormField, FormRoot, form } from '@angular/forms/signals';
import { LucideUpload } from '@lucide/angular';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Button } from '../../../ui/button';
import { Checkbox } from '../../../ui/checkbox';
import { CodeArea } from '../../../ui/code-area';
import { Dialog } from '../../../ui/dialog';
import { ForbiddenAlert } from '../forbidden-alert';
import { TEST_CASE_DATA_MAX_SIZE, testCaseDataRules } from './question-rules';
import {
  normalizeLineEndings,
  QuestionsApi,
  TEST_CASE_FORBIDDEN_MESSAGE,
  type TestCase,
  type TestCaseChanges,
  type TestCaseInput,
} from './questions-api';

type TestCaseModel = {
  input: string;
  output: string;
  isPublic: boolean;
};

type DataField = 'input' | 'output';

type FileStatus = {
  field: DataField;
  message: string;
  error: boolean;
};

export type TestCaseSaved = {
  testCase: TestCase;
  created: boolean;
};

const MAX_FILE_BYTES = 4 * TEST_CASE_DATA_MAX_SIZE;
const FILE_TOO_LARGE_MESSAGE = 'That file is too large for a test case.';
const FILE_UNREADABLE_MESSAGE = "That file couldn't be read.";

@Component({
  selector: 'cc-test-case-dialog',
  imports: [FormRoot, FormField, LucideUpload, Button, Checkbox, CodeArea, Dialog, ForbiddenAlert],
  templateUrl: './test-case-dialog.html',
})
export class TestCaseDialog {
  private readonly api = inject(QuestionsApi);
  private readonly injector = inject(Injector);
  private readonly inputFile = viewChild.required<ElementRef<HTMLInputElement>>('inputFile');
  private readonly outputFile = viewChild.required<ElementRef<HTMLInputElement>>('outputFile');

  readonly questionId = input.required<string>();
  readonly testCase = input<TestCase | null>(null);
  readonly number = input<number | null>(null);
  readonly saved = output<TestCaseSaved>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly dataMaxSize = TEST_CASE_DATA_MAX_SIZE;
  protected readonly creating = computed(() => this.testCase() === null);
  protected readonly title = computed(() => {
    const number = this.number();
    if (this.creating()) {
      return 'New test case';
    }
    return number === null ? 'Edit test case' : `Edit test case #${number}`;
  });
  protected readonly formError = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);
  protected readonly fileStatus = signal<FileStatus | null>(null);

  private readonly model = linkedSignal<TestCaseModel>(() => {
    const testCase = this.testCase();
    return {
      input: testCase?.input ?? '',
      output: testCase?.output ?? '',
      isPublic: testCase?.isPublic ?? false,
    };
  });
  protected readonly testCaseForm = form(
    this.model,
    (path) => {
      testCaseDataRules(path.input, 'Input');
      testCaseDataRules(path.output, 'Output');
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          this.missing.set([]);
          const value = field().value();
          const input: TestCaseInput = {
            input: normalizeLineEndings(value.input),
            output: normalizeLineEndings(value.output),
            isPublic: value.isPublic,
          };
          const current = this.testCase();
          try {
            if (current === null) {
              const testCase = await this.api.createTestCase(this.questionId(), input);
              this.saved.emit({ testCase, created: true });
              return undefined;
            }
            const changes = TestCaseDialog.changesFrom(current, input);
            if (Object.keys(changes).length === 0) {
              this.closed.emit();
              return undefined;
            }
            const testCase = await this.api.updateTestCase(this.questionId(), current.id, changes);
            this.saved.emit({ testCase, created: false });
            return undefined;
          } catch (error) {
            if (isGone(error)) {
              this.gone.emit();
              return undefined;
            }
            const failure = toSubmitFailure(
              error,
              { input: field.input, output: field.output },
              { FORBIDDEN: TEST_CASE_FORBIDDEN_MESSAGE },
            );
            this.formError.set(failure.formError);
            this.missing.set(missingPermissions(error));
            if (failure.fieldErrors.length > 0) {
              focusFirstInvalidAfterRender(this.injector, [field.input, field.output]);
            }
            return failure.fieldErrors;
          }
        },
        onInvalid: (field) => focusFirstInvalid([field.input, field.output]),
      },
    },
  );

  protected chooseFile(field: DataField): void {
    const element = field === 'input' ? this.inputFile() : this.outputFile();
    element.nativeElement.click();
  }

  protected async loadFile(field: DataField, event: Event): Promise<void> {
    const element = event.target as HTMLInputElement;
    const file = element.files?.[0];
    element.value = '';
    if (!file) {
      return;
    }
    if (file.size > MAX_FILE_BYTES) {
      this.fileStatus.set({ field, message: FILE_TOO_LARGE_MESSAGE, error: true });
      return;
    }
    try {
      const text = normalizeLineEndings(await file.text());
      this.model.update((model) => ({ ...model, [field]: text }));
      this.fieldFor(field)().markAsTouched();
      this.fileStatus.set({ field, message: `Loaded ${file.name}`, error: false });
    } catch {
      this.fileStatus.set({ field, message: FILE_UNREADABLE_MESSAGE, error: true });
    }
  }

  protected close(): void {
    if (!this.testCaseForm().submitting()) {
      this.closed.emit();
    }
  }

  private fieldFor(field: DataField): FieldTree<string> {
    return field === 'input' ? this.testCaseForm.input : this.testCaseForm.output;
  }

  private static changesFrom(testCase: TestCase, input: TestCaseInput): TestCaseChanges {
    const changes: TestCaseChanges = {};
    if (input.input !== testCase.input) {
      changes.input = input.input;
    }
    if (input.output !== testCase.output) {
      changes.output = input.output;
    }
    if (input.isPublic !== testCase.isPublic) {
      changes.isPublic = input.isPublic;
    }
    return changes;
  }
}

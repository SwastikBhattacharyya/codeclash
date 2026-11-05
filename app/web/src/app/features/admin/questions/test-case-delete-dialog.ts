import { Component, computed, inject, input, output, signal } from '@angular/core';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { ForbiddenAlert } from '../forbidden-alert';
import { QuestionsApi, TEST_CASE_FORBIDDEN_MESSAGE, type TestCase } from './questions-api';

const FORBIDDEN_CODE = 'FORBIDDEN';

@Component({
  selector: 'cc-test-case-delete-dialog',
  imports: [Button, Dialog, ForbiddenAlert],
  template: `
    <cc-dialog [title]="title()" (closed)="close()">
      @if (error(); as message) {
        <cc-forbidden-alert [message]="message" [permissions]="missing()" />
      }
      <p class="text-ink-muted">Submissions are no longer judged against it. This can't be undone.</p>
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="deleting()" (click)="close()">
          Cancel
        </button>
        <button ccButton variant="danger" type="button" [loading]="deleting()" (click)="confirm()">
          Delete test case
        </button>
      </div>
    </cc-dialog>
  `,
})
export class TestCaseDeleteDialog {
  private readonly api = inject(QuestionsApi);

  readonly questionId = input.required<string>();
  readonly testCase = input.required<TestCase>();
  readonly number = input<number | null>(null);
  readonly deleted = output<TestCase>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly title = computed(() => {
    const number = this.number();
    return number === null ? 'Delete test case?' : `Delete test case #${number}?`;
  });
  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);

  protected async confirm(): Promise<void> {
    const testCase = this.testCase();
    this.deleting.set(true);
    this.error.set(null);
    this.missing.set([]);
    try {
      await this.api.removeTestCase(this.questionId(), testCase.id);
      this.deleted.emit(testCase);
    } catch (error) {
      if (isGone(error)) {
        this.gone.emit();
        return;
      }
      this.error.set(
        toApiError(error)?.code === FORBIDDEN_CODE
          ? TEST_CASE_FORBIDDEN_MESSAGE
          : errorMessage(error),
      );
      this.missing.set(missingPermissions(error));
      this.deleting.set(false);
    }
  }

  protected close(): void {
    if (!this.deleting()) {
      this.closed.emit();
    }
  }
}

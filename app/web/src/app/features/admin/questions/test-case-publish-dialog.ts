import { Component, computed, DestroyRef, inject, input, output, signal } from '@angular/core';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { ForbiddenAlert } from '../forbidden-alert';
import { QuestionsApi, TEST_CASE_FORBIDDEN_MESSAGE } from './questions-api';

const FORBIDDEN_CODE = 'FORBIDDEN';

type Progress = {
  done: number;
  total: number;
};

@Component({
  selector: 'cc-test-case-publish-dialog',
  imports: [Button, Dialog, ForbiddenAlert],
  template: `
    <cc-dialog [title]="title()" (closed)="close()">
      @if (error(); as message) {
        <cc-forbidden-alert [message]="message" [permissions]="missing()" />
      }
      <p class="text-ink-muted">
        Anyone who can view this question will see their input and expected output.
      </p>
      <p class="sr-only" aria-live="polite">{{ status() }}</p>
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="running()" (click)="close()">
          Cancel
        </button>
        <button ccButton type="button" [loading]="running()" (click)="confirm()">
          {{ running() ? status() : 'Make all public' }}
        </button>
      </div>
    </cc-dialog>
  `,
})
export class TestCasePublishDialog {
  private readonly api = inject(QuestionsApi);
  private destroyed = false;

  readonly questionId = input.required<string>();
  readonly count = input.required<number>();
  readonly published = output<number>();
  readonly closed = output<void>();

  protected readonly running = signal(false);
  protected readonly progress = signal<Progress | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);

  protected readonly title = computed(() => {
    const count = this.progress()?.total ?? this.count();
    return count === 1
      ? 'Make 1 hidden test case public?'
      : `Make ${count} hidden test cases public?`;
  });
  protected readonly status = computed(() => {
    const progress = this.progress();
    return progress === null
      ? 'Making public…'
      : `Making public… ${progress.done} of ${progress.total}`;
  });

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.destroyed = true;
    });
  }

  protected async confirm(): Promise<void> {
    const questionId = this.questionId();
    this.running.set(true);
    this.error.set(null);
    this.missing.set([]);
    this.progress.set(null);
    let made = 0;
    let skipped = 0;
    let total = 0;
    try {
      const ids = await this.api.hiddenTestCaseIds(questionId);
      total = ids.length;
      for (const [index, id] of ids.entries()) {
        if (this.destroyed) {
          return;
        }
        this.progress.set({ done: index, total });
        try {
          await this.api.updateTestCase(questionId, id, { isPublic: true });
          made++;
        } catch (error) {
          if (!isGone(error)) {
            throw error;
          }
          skipped++;
        }
      }
      this.published.emit(made);
    } catch (error) {
      const reason =
        toApiError(error)?.code === FORBIDDEN_CODE
          ? TEST_CASE_FORBIDDEN_MESSAGE
          : errorMessage(error);
      const skippedNote = skipped > 0 ? ` (${skipped} no longer existed)` : '';
      this.error.set(
        total > 0 ? `Made ${made} of ${total} public${skippedNote}. ${reason}` : reason,
      );
      this.missing.set(missingPermissions(error));
      this.running.set(false);
    }
  }

  protected close(): void {
    if (!this.running()) {
      this.closed.emit();
    }
  }
}

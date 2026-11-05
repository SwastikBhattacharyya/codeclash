import { Component, computed, inject, input, output, signal } from '@angular/core';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { FieldIds } from '../../../ui/field-ids';
import { TextField } from '../../../ui/text-field';
import { ForbiddenAlert } from '../forbidden-alert';
import { type AdminQuestion, QUESTION_FORBIDDEN_MESSAGE, QuestionsApi } from './questions-api';

const FORBIDDEN_CODE = 'FORBIDDEN';
const RUNNING_CONTEST_CODE = 'QUESTION_IN_RUNNING_CONTEST';

@Component({
  selector: 'cc-question-delete-dialog',
  imports: [Button, Dialog, ForbiddenAlert, TextField],
  template: `
    <cc-dialog title="Delete question?" [describedBy]="warningId" (closed)="close()">
      @if (error(); as message) {
        <cc-forbidden-alert [message]="message" [permissions]="details()" />
      }
      <p [id]="warningId" class="break-words text-ink-muted">
        <strong class="font-semibold text-ink">{{ question().title }}</strong> and all of its test
        cases and submissions will be deleted. This can't be undone.
      </p>
      <cc-text-field
        label="Type the title to confirm"
        autocomplete="off"
        autocapitalize="none"
        [spellcheck]="false"
        [disabled]="deleting()"
        [(value)]="confirmation"
      />
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="deleting()" (click)="close()">
          Cancel
        </button>
        <button
          ccButton
          variant="danger"
          type="button"
          [disabled]="!confirmed()"
          [loading]="deleting()"
          (click)="confirm()"
        >
          Delete question
        </button>
      </div>
    </cc-dialog>
  `,
})
export class QuestionDeleteDialog {
  private readonly api = inject(QuestionsApi);

  readonly question = input.required<AdminQuestion>();
  readonly deleted = output<AdminQuestion>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly warningId = inject(FieldIds).create('question-delete-warning');
  protected readonly confirmation = signal('');
  protected readonly confirmed = computed(
    () => this.confirmation().trim() === this.question().title,
  );
  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly details = signal<readonly string[]>([]);

  protected async confirm(): Promise<void> {
    if (!this.confirmed()) {
      return;
    }
    const question = this.question();
    this.deleting.set(true);
    this.error.set(null);
    this.details.set([]);
    try {
      await this.api.remove(question.id);
      this.deleted.emit(question);
    } catch (error) {
      if (isGone(error)) {
        this.gone.emit();
        return;
      }
      const apiError = toApiError(error);
      if (apiError?.code === RUNNING_CONTEST_CODE) {
        const contestIds = apiError.properties?.['contestIds'];
        this.error.set("It's in a running contest and can't be deleted until the contest ends.");
        this.details.set(
          Array.isArray(contestIds)
            ? contestIds.filter((id): id is string => typeof id === 'string')
            : [],
        );
      } else {
        this.error.set(
          apiError?.code === FORBIDDEN_CODE ? QUESTION_FORBIDDEN_MESSAGE : errorMessage(error),
        );
        this.details.set(missingPermissions(error));
      }
      this.deleting.set(false);
    }
  }

  protected close(): void {
    if (!this.deleting()) {
      this.closed.emit();
    }
  }
}

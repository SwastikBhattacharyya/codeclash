import { Component, inject, input, output, signal } from '@angular/core';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import { type Submission, SubmissionsApi } from '../../../core/submissions/submissions';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { ForbiddenAlert } from '../forbidden-alert';
import { SUBMISSION_FORBIDDEN_MESSAGE } from './submissions-api';

const FORBIDDEN_CODE = 'FORBIDDEN';

@Component({
  selector: 'cc-submission-delete-dialog',
  imports: [Button, Dialog, ForbiddenAlert],
  template: `
    <cc-dialog title="Delete submission?" (closed)="close()">
      @if (error(); as message) {
        <cc-forbidden-alert [message]="message" [permissions]="missing()" />
      }
      <p class="text-ink-muted">
        The submission by
        <strong class="font-semibold text-ink">&#64;{{ submission().username }}</strong> will be
        deleted. If it was accepted, contest standings are recalculated. This can't be undone.
      </p>
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="deleting()" (click)="close()">
          Cancel
        </button>
        <button ccButton variant="danger" type="button" [loading]="deleting()" (click)="confirm()">
          Delete submission
        </button>
      </div>
    </cc-dialog>
  `,
})
export class SubmissionDeleteDialog {
  private readonly api = inject(SubmissionsApi);

  readonly submission = input.required<Submission>();
  readonly deleted = output<Submission>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);

  protected async confirm(): Promise<void> {
    const submission = this.submission();
    this.deleting.set(true);
    this.error.set(null);
    this.missing.set([]);
    try {
      await this.api.remove(submission.id);
      this.deleted.emit(submission);
    } catch (error) {
      if (isGone(error)) {
        this.gone.emit();
        return;
      }
      this.error.set(
        toApiError(error)?.code === FORBIDDEN_CODE
          ? SUBMISSION_FORBIDDEN_MESSAGE
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

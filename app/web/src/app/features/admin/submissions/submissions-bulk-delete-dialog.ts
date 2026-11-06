import { Component, computed, inject, input, output, signal } from '@angular/core';
import { isGone } from '../../../core/api/gone';
import { type Submission, SubmissionsApi } from '../../../core/submissions/submissions';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { FieldIds } from '../../../ui/field-ids';

export type BulkDeleteResult = {
  deleted: readonly string[];
  failed: readonly string[];
};

@Component({
  selector: 'cc-submissions-bulk-delete-dialog',
  imports: [Button, Dialog],
  template: `
    <cc-dialog [title]="title()" [describedBy]="descriptionId" (closed)="close()">
      <p [id]="descriptionId" class="text-ink-muted">
        {{ countLabel() }} will be deleted. Contest standings are recalculated for any that were
        accepted. This can't be undone.
      </p>
      <p class="text-sm text-ink-muted tabular-nums" aria-live="polite">
        @if (running()) {
          Deleting {{ progress() }} of {{ submissions().length }}…
        }
      </p>
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="running()" (click)="close()">
          Cancel
        </button>
        <button ccButton variant="danger" type="button" [loading]="running()" (click)="confirm()">
          Delete {{ submissions().length }}
        </button>
      </div>
    </cc-dialog>
  `,
})
export class SubmissionsBulkDeleteDialog {
  private readonly api = inject(SubmissionsApi);

  readonly submissions = input.required<readonly Submission[]>();
  readonly finished = output<BulkDeleteResult>();
  readonly closed = output<void>();

  protected readonly descriptionId = inject(FieldIds).create('bulk-delete-description');
  protected readonly running = signal(false);
  protected readonly done = signal(0);
  protected readonly progress = computed(() =>
    Math.min(this.done() + 1, this.submissions().length),
  );
  protected readonly countLabel = computed(() => {
    const count = this.submissions().length;
    return count === 1 ? '1 submission' : `${count} submissions`;
  });
  protected readonly title = computed(() => `Delete ${this.countLabel()}?`);

  protected async confirm(): Promise<void> {
    this.running.set(true);
    this.done.set(0);
    const deleted: string[] = [];
    const failed: string[] = [];
    for (const submission of this.submissions()) {
      try {
        await this.api.remove(submission.id);
        deleted.push(submission.id);
      } catch (error) {
        if (isGone(error)) {
          deleted.push(submission.id);
        } else {
          failed.push(submission.id);
        }
      }
      this.done.update((count) => count + 1);
    }
    this.finished.emit({ deleted, failed });
  }

  protected close(): void {
    if (!this.running()) {
      this.closed.emit();
    }
  }
}

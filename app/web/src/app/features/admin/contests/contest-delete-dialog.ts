import { Component, computed, inject, input, output, signal } from '@angular/core';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import type { ContestSummary } from '../../../core/contests/contests-api';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { FieldIds } from '../../../ui/field-ids';
import { TextField } from '../../../ui/text-field';
import { ForbiddenAlert } from '../forbidden-alert';
import { AdminContestsApi, CONTEST_ALREADY_LIVE, CONTEST_FORBIDDEN_MESSAGE } from './contests-api';

const FORBIDDEN_CODE = 'FORBIDDEN';
const ALREADY_LIVE_MESSAGE = "It has already started, so it can't be deleted.";

@Component({
  selector: 'cc-contest-delete-dialog',
  imports: [Button, Dialog, ForbiddenAlert, TextField],
  template: `
    <cc-dialog title="Delete contest?" [describedBy]="warningId" (closed)="close()">
      @if (error(); as message) {
        <cc-forbidden-alert [message]="message" [permissions]="details()" />
      }
      <p [id]="warningId" class="break-words text-ink-muted">
        <strong class="font-semibold text-ink">{{ contest().name }}</strong> and all of its
        registrations will be deleted. This can't be undone.
      </p>
      <cc-text-field
        label="Type the name to confirm"
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
          Delete contest
        </button>
      </div>
    </cc-dialog>
  `,
})
export class ContestDeleteDialog {
  private readonly api = inject(AdminContestsApi);

  readonly contest = input.required<ContestSummary>();
  readonly deleted = output<ContestSummary>();
  readonly gone = output<void>();
  readonly blocked = output<void>();
  readonly closed = output<void>();

  protected readonly warningId = inject(FieldIds).create('contest-delete-warning');
  protected readonly confirmation = signal('');
  protected readonly confirmed = computed(() => this.confirmation().trim() === this.contest().name);
  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly details = signal<readonly string[]>([]);

  protected async confirm(): Promise<void> {
    if (!this.confirmed()) {
      return;
    }
    const contest = this.contest();
    this.deleting.set(true);
    this.error.set(null);
    this.details.set([]);
    try {
      await this.api.remove(contest.id);
      this.deleted.emit(contest);
    } catch (error) {
      if (isGone(error)) {
        this.gone.emit();
        return;
      }
      const code = toApiError(error)?.code;
      if (code === CONTEST_ALREADY_LIVE) {
        this.error.set(ALREADY_LIVE_MESSAGE);
        this.blocked.emit();
      } else if (code === FORBIDDEN_CODE) {
        this.error.set(CONTEST_FORBIDDEN_MESSAGE);
      } else {
        this.error.set(errorMessage(error));
      }
      this.details.set(missingPermissions(error));
      this.deleting.set(false);
    }
  }

  protected close(): void {
    if (!this.deleting()) {
      this.closed.emit();
    }
  }
}

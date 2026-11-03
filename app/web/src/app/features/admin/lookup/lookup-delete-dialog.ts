import { Component, inject, input, output, signal } from '@angular/core';
import { errorMessage } from '../../../core/api/api-errors';
import { isGone } from '../../../core/api/gone';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { LookupApi, type LookupItem } from './lookup-api';
import type { LookupConfig } from './lookup-config';

@Component({
  selector: 'cc-lookup-delete-dialog',
  imports: [Alert, Button, Dialog],
  template: `
    <cc-dialog [title]="'Delete ' + config().noun + '?'" (closed)="close()">
      @if (error(); as message) {
        <cc-alert tone="error">{{ message }}</cc-alert>
      }
      <p class="text-ink-muted">
        <strong class="font-semibold text-ink">{{ item().name }}</strong> will be removed from
        every question that uses it. This can't be undone.
      </p>
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="deleting()" (click)="close()">
          Cancel
        </button>
        <button ccButton variant="danger" type="button" [loading]="deleting()" (click)="confirm()">
          Delete {{ config().noun }}
        </button>
      </div>
    </cc-dialog>
  `,
})
export class LookupDeleteDialog {
  private readonly api = inject(LookupApi);

  readonly config = input.required<LookupConfig>();
  readonly item = input.required<LookupItem>();
  readonly deleted = output<LookupItem>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected async confirm(): Promise<void> {
    const item = this.item();
    this.deleting.set(true);
    this.error.set(null);
    try {
      await this.api.remove(this.config().url, item.id);
      this.deleted.emit(item);
    } catch (error) {
      if (isGone(error)) {
        this.gone.emit();
        return;
      }
      this.error.set(errorMessage(error));
      this.deleting.set(false);
    }
  }

  protected close(): void {
    if (!this.deleting()) {
      this.closed.emit();
    }
  }
}

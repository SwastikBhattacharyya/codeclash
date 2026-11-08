import { Component, inject, input, output, signal } from '@angular/core';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { ForbiddenAlert } from '../forbidden-alert';
import { type AdminUser, USER_FORBIDDEN_MESSAGE, UsersApi } from './users-api';

const FORBIDDEN_CODE = 'FORBIDDEN';

@Component({
  selector: 'cc-user-delete-dialog',
  imports: [Button, Dialog, ForbiddenAlert],
  template: `
    <cc-dialog [title]="'Delete @' + user().username + '?'" (closed)="close()">
      @if (error(); as message) {
        <cc-forbidden-alert [message]="message" [permissions]="missing()" />
      }
      <p class="text-ink-muted">
        <strong class="font-semibold text-ink">{{ user().name }}</strong> will be signed out
        everywhere. This can't be undone.
      </p>
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="deleting()" (click)="close()">
          Cancel
        </button>
        <button ccButton variant="danger" type="button" [loading]="deleting()" (click)="confirm()">
          Delete user
        </button>
      </div>
    </cc-dialog>
  `,
})
export class UserDeleteDialog {
  private readonly api = inject(UsersApi);

  readonly user = input.required<AdminUser>();
  readonly deleted = output<AdminUser>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);

  protected async confirm(): Promise<void> {
    const user = this.user();
    this.deleting.set(true);
    this.error.set(null);
    this.missing.set([]);
    try {
      await this.api.remove(user.id);
      this.deleted.emit(user);
    } catch (error) {
      if (isGone(error)) {
        this.gone.emit();
        return;
      }
      this.error.set(
        toApiError(error)?.code === FORBIDDEN_CODE ? USER_FORBIDDEN_MESSAGE : errorMessage(error),
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

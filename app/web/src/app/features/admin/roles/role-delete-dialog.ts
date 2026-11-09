import { httpResource } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { errorMessage, toApiError } from '../../../core/api/api-errors';
import { missingPermissions } from '../../../core/api/forbidden';
import { isGone } from '../../../core/api/gone';
import type { Page } from '../../../core/api/page';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { FieldIds } from '../../../ui/field-ids';
import { Spinner } from '../../../ui/spinner';
import { ForbiddenAlert } from '../forbidden-alert';
import { RoleAccess } from './role-access';
import { type AdminRole, ROLE_FORBIDDEN_MESSAGE, RolesApi, roleMembersRequest } from './roles-api';

const FORBIDDEN_CODE = 'FORBIDDEN';
const GENERIC_IMPACT = 'Users with this role will be moved to the default role.';

@Component({
  selector: 'cc-role-delete-dialog',
  imports: [Button, Dialog, ForbiddenAlert, Spinner],
  template: `
    <cc-dialog [title]="'Delete ' + role().name + '?'" [describedBy]="impactId" (closed)="close()">
      @if (error(); as message) {
        <cc-forbidden-alert [message]="message" [permissions]="missing()" />
      }
      <p [id]="impactId" class="text-ink-muted">
        @if (impact(); as text) {
          {{ text }} This can't be undone.
        } @else {
          <span class="inline-flex items-center gap-2">
            <cc-spinner class="size-4" />
            Checking who has this role
          </span>
        }
      </p>
      <div class="flex flex-wrap justify-end gap-3">
        <button ccButton variant="ghost" type="button" [disabled]="deleting()" (click)="close()">
          Cancel
        </button>
        <button ccButton variant="danger" type="button" [loading]="deleting()" (click)="confirm()">
          Delete role
        </button>
      </div>
    </cc-dialog>
  `,
})
export class RoleDeleteDialog {
  private readonly api = inject(RolesApi);
  private readonly access = inject(RoleAccess);

  readonly role = input.required<AdminRole>();
  readonly deleted = output<AdminRole>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly impactId = inject(FieldIds).create('role-delete-impact');
  protected readonly deleting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly missing = signal<readonly string[]>([]);

  private readonly members = httpResource<Page<unknown>>(() =>
    this.access.canCountMembers() ? roleMembersRequest(this.role().id) : undefined,
  );
  protected readonly impact = computed(() => {
    if (this.members.isLoading()) {
      return null;
    }
    if (!this.members.hasValue()) {
      return GENERIC_IMPACT;
    }
    const count = this.members.value().items;
    if (count === 0) {
      return 'No users have this role.';
    }
    return count === 1
      ? '1 user will be moved to the default role.'
      : `${count} users will be moved to the default role.`;
  });

  protected async confirm(): Promise<void> {
    const role = this.role();
    this.deleting.set(true);
    this.error.set(null);
    this.missing.set([]);
    try {
      await this.api.remove(role.id);
      this.deleted.emit(role);
    } catch (error) {
      if (isGone(error)) {
        this.gone.emit();
        return;
      }
      this.error.set(
        toApiError(error)?.code === FORBIDDEN_CODE ? ROLE_FORBIDDEN_MESSAGE : errorMessage(error),
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

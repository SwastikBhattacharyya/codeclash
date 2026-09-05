import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  LucideChevronDown,
  LucideCircleUser,
  LucideKeyRound,
  LucideLogOut,
  LucideShield,
  LucideUser,
} from '@lucide/angular';
import { ADMIN_PATH, hasAdminAccess } from '../core/admin/admin-access';
import { ACCOUNT_PREFIX, CHANGE_PASSWORD_PATH, PROFILE_PATH } from '../core/auth/auth-paths';
import { AuthStore } from '../core/auth/auth-store';
import { PROBLEMS_PATH } from '../core/problems/problems-api';
import { FieldIds } from './field-ids';
import { ToastStore } from './toast-store';

@Component({
  selector: 'cc-user-menu',
  imports: [
    RouterLink,
    LucideChevronDown,
    LucideCircleUser,
    LucideKeyRound,
    LucideLogOut,
    LucideShield,
    LucideUser,
  ],
  host: {
    class: 'relative block',
    '(document:click)': 'onDocumentClick($event)',
    '(document:keydown.escape)': 'closeAndFocusTrigger()',
    '(focusout)': 'onFocusOut($event)',
  },
  templateUrl: './user-menu.html',
})
export class UserMenu {
  private readonly auth = inject(AuthStore);
  private readonly toasts = inject(ToastStore);
  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly trigger = viewChild.required<ElementRef<HTMLButtonElement>>('trigger');

  protected readonly menuId = inject(FieldIds).create('menu');
  protected readonly open = signal(false);
  protected readonly signingOut = signal(false);
  protected readonly profilePath = PROFILE_PATH;
  protected readonly changePasswordPath = CHANGE_PASSWORD_PATH;
  protected readonly adminPath = ADMIN_PATH;
  protected readonly isAdmin = computed(() =>
    hasAdminAccess(this.auth.session()?.permissions ?? []),
  );

  protected toggle(): void {
    this.open.update((open) => !open);
  }

  protected closeAfterNavigation(): void {
    this.open.set(false);
    this.trigger().nativeElement.focus();
  }

  protected onFocusOut(event: FocusEvent): void {
    const next = event.relatedTarget as Node | null;
    if (this.open() && next !== null && !this.host.nativeElement.contains(next)) {
      this.open.set(false);
    }
  }

  protected closeAndFocusTrigger(): void {
    if (!this.open()) {
      return;
    }
    this.open.set(false);
    this.trigger().nativeElement.focus();
  }

  protected onDocumentClick(event: MouseEvent): void {
    if (this.open() && !this.host.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }

  protected async signOut(): Promise<void> {
    this.signingOut.set(true);
    try {
      await this.auth.signOut();
      this.open.set(false);
      if (this.router.url.startsWith(ACCOUNT_PREFIX) || this.router.url.startsWith(ADMIN_PATH)) {
        await this.router.navigateByUrl(PROBLEMS_PATH);
      }
      this.toasts.show('Signed out');
    } catch {
      this.toasts.show('Sign out failed. Try again.', 'error');
    } finally {
      this.signingOut.set(false);
    }
  }
}

import { Component, computed, type ElementRef, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  type ActivatedRouteSnapshot,
  NavigationEnd,
  Router,
  RouterLink,
  RouterOutlet,
} from '@angular/router';
import { LucideMenu, LucideX } from '@lucide/angular';
import { filter, map } from 'rxjs';
import { SIGN_IN_PATH, SIGN_UP_PATH } from '../core/auth/auth-paths';
import { AuthStore } from '../core/auth/auth-store';
import { CONTESTS_PATH } from '../core/contests/contests-api';
import { PROBLEM_PATH, PROBLEMS_PATH } from '../core/problems/problems-api';
import { Button } from '../ui/button';
import { FieldIds } from '../ui/field-ids';
import { ThemeToggle } from '../ui/theme-toggle';
import { UserMenu } from '../ui/user-menu';
import { Wordmark } from '../ui/wordmark';

export const FULL_WIDTH_DATA_KEY = 'fullWidth';

type NavLink = {
  label: string;
  path: string;
  active: boolean;
};

@Component({
  selector: 'cc-app-layout',
  imports: [RouterOutlet, RouterLink, LucideMenu, LucideX, Button, ThemeToggle, UserMenu, Wordmark],
  host: {
    '(document:keydown.escape)': 'closeMenuAndFocusToggle()',
  },
  templateUrl: './app-layout.html',
})
export class AppLayout {
  private readonly router = inject(Router);
  private readonly menuToggle = viewChild<ElementRef<HTMLButtonElement>>('menuToggle');

  protected readonly auth = inject(AuthStore);
  protected readonly menuId = inject(FieldIds).create('site-menu');
  protected readonly menuOpen = signal(false);
  protected readonly signInPath = SIGN_IN_PATH;
  protected readonly signUpPath = SIGN_UP_PATH;
  protected readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );
  protected readonly fullWidth = computed(() => {
    this.currentUrl();
    return AppLayout.wantsFullWidth(this.router.routerState.snapshot.root);
  });
  protected readonly signInParams = computed(() => ({ redirect: this.currentUrl() }));
  protected readonly links = computed<NavLink[]>(() => {
    const path = this.currentUrl().split(/[?#]/, 1)[0] ?? '';
    return [
      {
        label: 'Problems',
        path: PROBLEMS_PATH,
        active: path === PROBLEMS_PATH || path.startsWith(`${PROBLEM_PATH}/`),
      },
      {
        label: 'Contests',
        path: CONTESTS_PATH,
        active: path === CONTESTS_PATH || path.startsWith(`${CONTESTS_PATH}/`),
      },
    ];
  });

  constructor() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.menuOpen.set(false));
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  protected closeMenuAndFocusToggle(): void {
    if (!this.menuOpen()) {
      return;
    }
    this.menuOpen.set(false);
    this.menuToggle()?.nativeElement.focus();
  }

  private static wantsFullWidth(route: ActivatedRouteSnapshot): boolean {
    let current: ActivatedRouteSnapshot | null = route;
    while (current !== null) {
      if (current.data[FULL_WIDTH_DATA_KEY] === true) {
        return true;
      }
      current = current.firstChild;
    }
    return false;
  }
}

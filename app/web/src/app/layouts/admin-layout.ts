import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  type ElementRef,
  Injector,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import {
  NavigationEnd,
  NavigationSkipped,
  Router,
  RouterLink,
  RouterOutlet,
} from '@angular/router';
import { LucideArrowLeft, LucideLayoutDashboard, LucideMenu, LucideX } from '@lucide/angular';
import { filter, map } from 'rxjs';
import { ADMIN_PATH } from '../core/admin/admin-access';
import { type AdminSection, allowedGroups } from '../core/admin/admin-sections';
import { AuthStore } from '../core/auth/auth-store';
import { PROBLEMS_PATH } from '../core/problems/problems-api';
import { AdminSectionIcon } from '../ui/admin-section-icon';
import { FieldIds } from '../ui/field-ids';
import { ThemeToggle } from '../ui/theme-toggle';
import { UserMenu } from '../ui/user-menu';
import { Wordmark } from '../ui/wordmark';

const DESKTOP_QUERY = '(width >= 64rem)';
const OVERVIEW_LABEL = 'Overview';

@Component({
  selector: 'cc-admin-layout',
  imports: [
    AdminSectionIcon,
    RouterOutlet,
    RouterLink,
    LucideArrowLeft,
    LucideLayoutDashboard,
    LucideMenu,
    LucideX,
    ThemeToggle,
    UserMenu,
    Wordmark,
  ],
  host: {
    '(document:keydown.escape)': 'closeDrawerAndFocusToggle()',
  },
  templateUrl: './admin-layout.html',
})
export class AdminLayout {
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly auth = inject(AuthStore);
  private readonly drawerToggle = viewChild<ElementRef<HTMLButtonElement>>('drawerToggle');
  private readonly drawerClose = viewChild<ElementRef<HTMLButtonElement>>('drawerClose');
  private readonly content = viewChild.required<ElementRef<HTMLElement>>('content');

  protected readonly drawerId = inject(FieldIds).create('admin-nav');
  protected readonly drawerOpen = signal(false);
  protected readonly adminPath = ADMIN_PATH;
  protected readonly sitePath = PROBLEMS_PATH;
  protected readonly overviewLabel = OVERVIEW_LABEL;
  protected readonly groups = computed(() => allowedGroups(this.auth.session()?.permissions ?? []));
  private readonly currentPath = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => AdminLayout.pathOf(event.urlAfterRedirects)),
    ),
    { initialValue: AdminLayout.pathOf(this.router.url) },
  );
  protected readonly overviewActive = computed(() => this.currentPath() === ADMIN_PATH);
  protected readonly currentLabel = computed(() => {
    const path = this.currentPath();
    const section = this.groups()
      .flatMap((group) => group.sections)
      .find((candidate) => AdminLayout.matches(path, candidate));
    return section?.label ?? OVERVIEW_LABEL;
  });

  constructor() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd || event instanceof NavigationSkipped),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.closeDrawerAndFocusContent());

    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const desktop = window.matchMedia(DESKTOP_QUERY);
      const onChange = (event: MediaQueryListEvent): void => {
        if (event.matches) {
          this.drawerOpen.set(false);
        }
      };
      desktop.addEventListener('change', onChange);
      destroyRef.onDestroy(() => desktop.removeEventListener('change', onChange));
    });
  }

  protected isActive(section: AdminSection): boolean {
    return AdminLayout.matches(this.currentPath(), section);
  }

  protected openDrawer(): void {
    this.drawerOpen.set(true);
    afterNextRender(() => this.drawerClose()?.nativeElement.focus(), {
      injector: this.injector,
    });
  }

  protected closeDrawerAndFocusToggle(): void {
    if (!this.drawerOpen()) {
      return;
    }
    this.drawerOpen.set(false);
    afterNextRender(() => this.drawerToggle()?.nativeElement.focus(), {
      injector: this.injector,
    });
  }

  private closeDrawerAndFocusContent(): void {
    if (!this.drawerOpen()) {
      return;
    }
    this.drawerOpen.set(false);
    afterNextRender(() => this.content().nativeElement.focus(), { injector: this.injector });
  }

  private static pathOf(url: string): string {
    return url.split(/[?#]/, 1)[0] ?? '';
  }

  private static matches(path: string, section: AdminSection): boolean {
    return path === section.path || path.startsWith(`${section.path}/`);
  }
}

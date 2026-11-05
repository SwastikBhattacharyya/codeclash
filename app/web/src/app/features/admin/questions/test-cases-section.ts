import { httpResource } from '@angular/common/http';
import {
  afterNextRender,
  Component,
  computed,
  type ElementRef,
  Injector,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { LucideEye, LucidePencil, LucidePlus, LucideTrash } from '@lucide/angular';
import {
  TEST_CASE_READ_IS_PRIVATE,
  TEST_CASE_WRITE,
  TEST_CASE_WRITE_IS_PRIVATE,
} from '../../../core/admin/admin-access';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import { AuthStore } from '../../../core/auth/auth-store';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { DataTable, type TableColumn, TableRow } from '../../../ui/data-table';
import { FIRST_PAGE, Pager } from '../../../ui/pager';
import { Select, type SelectOption } from '../../../ui/select';
import { ToastStore } from '../../../ui/toast-store';
import {
  hiddenTestCaseCountRequest,
  TEST_CASE_PAGE_SIZE,
  TEST_CASE_VISIBILITIES,
  type TestCase,
  type TestCaseQuery,
  testCasesRequest,
} from './questions-api';
import { TestCaseDeleteDialog } from './test-case-delete-dialog';
import { TestCaseDialog, type TestCaseSaved } from './test-case-dialog';
import { TestCasePublishDialog } from './test-case-publish-dialog';

type EditTarget = {
  testCase: TestCase | null;
};

const PAGE_PARAM = 'tc_page';
const VISIBILITY_PARAM = 'tc_visibility';
const PREVIEW_LENGTH = 48;
const VISIBILITY_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  { value: 'public', label: 'Public' },
  { value: 'hidden', label: 'Hidden' },
];

@Component({
  selector: 'cc-test-cases-section',
  imports: [
    LucideEye,
    LucidePencil,
    LucidePlus,
    LucideTrash,
    Alert,
    Button,
    DataTable,
    Pager,
    Select,
    TableRow,
    TestCaseDeleteDialog,
    TestCaseDialog,
    TestCasePublishDialog,
  ],
  host: { class: 'flex flex-col gap-4' },
  templateUrl: './test-cases-section.html',
})
export class TestCasesSection {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toasts = inject(ToastStore);
  private readonly injector = inject(Injector);
  private readonly auth = inject(AuthStore);
  private readonly addButton = viewChild<ElementRef<HTMLButtonElement>>('addButton');
  private readonly params = toSignal(this.route.queryParamMap, { requireSync: true });

  readonly questionId = input.required<string>();

  protected readonly pageParam = PAGE_PARAM;
  protected readonly visibilityOptions = VISIBILITY_OPTIONS;
  protected readonly canWrite = computed(
    () => this.auth.session()?.permissions.includes(TEST_CASE_WRITE) ?? false,
  );
  protected readonly canPublishAll = computed(() => {
    const permissions = this.auth.session()?.permissions ?? [];
    return [TEST_CASE_WRITE, TEST_CASE_WRITE_IS_PRIVATE, TEST_CASE_READ_IS_PRIVATE].every(
      (permission) => permissions.includes(permission),
    );
  });
  protected readonly columns: readonly TableColumn[] = [
    { label: '#', sticky: 'start' },
    { label: 'Visibility' },
    { label: 'Input' },
    { label: 'Expected output' },
    { label: 'Actions', sticky: 'end' },
  ];

  protected readonly query = computed<TestCaseQuery>(() => {
    const params = this.params();
    const page = Number.parseInt(params.get(PAGE_PARAM) ?? '', 10);
    const visibility = TEST_CASE_VISIBILITIES.find(
      (candidate) => candidate === params.get(VISIBILITY_PARAM),
    );
    return {
      visibility: visibility ?? null,
      page: Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE,
    };
  });
  protected readonly testCases = httpResource<Page<TestCase>>(() =>
    testCasesRequest(this.questionId(), this.query()),
  );
  protected readonly shownTestCases = lastValue(this.testCases);
  private readonly hiddenTestCases = httpResource<Page<TestCase>>(() =>
    this.canPublishAll() ? hiddenTestCaseCountRequest(this.questionId()) : undefined,
  );
  protected readonly hiddenCount = computed(() =>
    this.hiddenTestCases.hasValue() ? this.hiddenTestCases.value().items : null,
  );
  protected readonly rows = computed(() => this.shownTestCases()?.data ?? []);
  private readonly numbers = computed(() => {
    const page = this.shownTestCases();
    const offset = page ? (page.page - 1) * TEST_CASE_PAGE_SIZE : 0;
    return new Map(this.rows().map((testCase, index) => [testCase.id, offset + index + 1]));
  });
  protected readonly total = computed(() => this.shownTestCases()?.items ?? null);
  protected readonly emptyText = computed(() => {
    if (this.query().visibility !== null) {
      return 'No test cases match this filter';
    }
    if (this.query().page > FIRST_PAGE) {
      return 'This page is empty';
    }
    return 'No test cases yet';
  });

  protected readonly editTarget = signal<EditTarget | null>(null);
  protected readonly deleteTarget = signal<TestCase | null>(null);
  protected readonly publishOpen = signal(false);

  protected numberOf(testCase: TestCase): number | null {
    return this.numbers().get(testCase.id) ?? null;
  }

  protected preview(value: string): string {
    if (value === '') {
      return '(empty)';
    }
    const newline = value.indexOf('\n');
    const firstLine = newline === -1 ? value : value.slice(0, newline);
    const more = newline !== -1 && newline < value.length - 1;
    const clipped =
      firstLine.length > PREVIEW_LENGTH ? `${firstLine.slice(0, PREVIEW_LENGTH)}…` : firstLine;
    return more ? `${clipped} ⏎ …` : clipped;
  }

  protected openAdd(): void {
    this.editTarget.set({ testCase: null });
  }

  protected openEdit(testCase: TestCase): void {
    this.editTarget.set({ testCase });
  }

  protected openDelete(testCase: TestCase): void {
    this.deleteTarget.set(testCase);
  }

  protected onPublished(count: number): void {
    this.publishOpen.set(false);
    if (count === 0) {
      this.toasts.show('No hidden test cases were left to make public', 'error');
    } else {
      this.toasts.show(count === 1 ? 'Made 1 test case public' : `Made ${count} test cases public`);
    }
    this.reloadAll();
  }

  protected onPublishClosed(): void {
    this.publishOpen.set(false);
    this.reloadAll();
  }

  protected onVisibilityFilter(visibility: string): void {
    void this.navigate({
      [VISIBILITY_PARAM]: visibility === '' ? null : visibility,
      [PAGE_PARAM]: null,
    });
  }

  protected onSaved(saved: TestCaseSaved): void {
    this.editTarget.set(null);
    this.toasts.show(saved.created ? 'Test case added' : 'Test case saved');
    this.reloadAll();
  }

  protected onDeleted(): void {
    this.deleteTarget.set(null);
    this.toasts.show('Test case deleted');
    this.refreshAfterRemoval();
  }

  protected onGone(): void {
    this.editTarget.set(null);
    this.deleteTarget.set(null);
    this.toasts.show('That test case no longer exists', 'error');
    this.refreshAfterRemoval();
  }

  private reloadAll(): void {
    this.testCases.reload();
    this.hiddenTestCases.reload();
  }

  private refreshAfterRemoval(): void {
    afterNextRender(() => this.addButton()?.nativeElement.focus(), { injector: this.injector });
    const page = this.shownTestCases();
    if (page && page.page > FIRST_PAGE && page.data.length <= 1) {
      const previous = page.page - 1;
      void this.navigate({ [PAGE_PARAM]: previous === FIRST_PAGE ? null : String(previous) });
      this.hiddenTestCases.reload();
      return;
    }
    this.reloadAll();
  }

  private navigate(queryParams: Record<string, string | null>): Promise<boolean> {
    return this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }
}

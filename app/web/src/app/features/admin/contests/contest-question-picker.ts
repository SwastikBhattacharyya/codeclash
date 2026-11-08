import { httpResource } from '@angular/common/http';
import {
  Component,
  computed,
  type ElementRef,
  effect,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { debounceTime } from 'rxjs';
import { QUESTION_READ_IS_PRIVATE } from '../../../core/admin/admin-access';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import { AuthStore } from '../../../core/auth/auth-store';
import { QUESTIONS_URL, type Question } from '../../../core/problems/problems-api';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { FieldIds } from '../../../ui/field-ids';
import { FIRST_PAGE } from '../../../ui/pager';
import { Select, type SelectOption } from '../../../ui/select';
import { Spinner } from '../../../ui/spinner';
import { TextField } from '../../../ui/text-field';

export type PickedQuestion = {
  id: string;
  title: string;
};

type Visibility = '' | 'visible' | 'hidden';

const PICKER_PAGE_SIZE = 10;
const SEARCH_DEBOUNCE_MS = 300;
const VISIBILITY_OPTIONS: readonly SelectOption[] = [
  { value: '', label: 'All' },
  { value: 'visible', label: 'Published' },
  { value: 'hidden', label: 'Draft' },
];

@Component({
  selector: 'cc-contest-question-picker',
  imports: [Alert, Button, Dialog, Select, Spinner, TextField],
  templateUrl: './contest-question-picker.html',
})
export class ContestQuestionPicker {
  private readonly auth = inject(AuthStore);
  private readonly list = viewChild<ElementRef<HTMLElement>>('list');

  readonly excluded = input.required<ReadonlySet<string>>();
  readonly added = output<readonly PickedQuestion[]>();
  readonly closed = output<void>();

  protected readonly descriptionId = inject(FieldIds).create('question-picker-description');
  protected readonly visibilityOptions = VISIBILITY_OPTIONS;
  protected readonly firstPage = FIRST_PAGE;
  protected readonly canReadPrivate = computed(
    () => this.auth.session()?.permissions.includes(QUESTION_READ_IS_PRIVATE) ?? false,
  );

  protected readonly searchText = signal('');
  private readonly title = signal('');
  protected readonly visibility = signal<Visibility>('');
  protected readonly page = signal(FIRST_PAGE);

  protected readonly questions = httpResource<Page<Question>>(() => {
    const params: Record<string, string> = {
      page: String(this.page()),
      size: String(PICKER_PAGE_SIZE),
      sort: 'title',
      direction: 'asc',
    };
    const title = this.title();
    if (title !== '') {
      params['title'] = title;
    }
    const visibility = this.visibility();
    if (visibility !== '' && this.canReadPrivate()) {
      params['visible'] = String(visibility === 'visible');
    }
    return { url: QUESTIONS_URL, params };
  });
  protected readonly shownQuestions = lastValue(this.questions);
  protected readonly rows = computed(() => this.shownQuestions()?.data ?? []);
  protected readonly selected = signal<ReadonlyMap<string, PickedQuestion>>(new Map());
  protected readonly selectedCount = computed(() => this.selected().size);
  protected readonly confirmLabel = computed(() => {
    const count = this.selectedCount();
    return count === 1 ? 'Add 1 question' : `Add ${count} questions`;
  });

  constructor() {
    effect(() => {
      this.page();
      this.title();
      this.visibility();
      const list = this.list();
      if (list) {
        list.nativeElement.scrollTop = 0;
      }
    });
    toObservable(this.searchText)
      .pipe(debounceTime(SEARCH_DEBOUNCE_MS), takeUntilDestroyed())
      .subscribe((text) => {
        const trimmed = text.trim();
        if (trimmed !== this.title()) {
          this.title.set(trimmed);
          this.page.set(FIRST_PAGE);
        }
      });
  }

  protected isAdded(question: Question): boolean {
    return this.excluded().has(question.id);
  }

  protected isSelected(question: Question): boolean {
    return this.isAdded(question) || this.selected().has(question.id);
  }

  protected toggle(question: Question, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.selected.update((current) => {
      const next = new Map(current);
      if (checked) {
        next.set(question.id, { id: question.id, title: question.title });
      } else {
        next.delete(question.id);
      }
      return next;
    });
  }

  protected onVisibility(value: string): void {
    this.visibility.set(value === 'visible' || value === 'hidden' ? value : '');
    this.page.set(FIRST_PAGE);
  }

  protected goToPage(page: number): void {
    this.page.set(Math.max(FIRST_PAGE, page));
  }

  protected confirm(): void {
    if (this.selectedCount() > 0) {
      this.added.emit([...this.selected().values()]);
    }
  }
}

import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Button } from './button';

export const FIRST_PAGE = 1;

@Component({
  selector: 'cc-pager',
  imports: [RouterLink, Button],
  host: { class: 'block' },
  template: `
    @if (page() > firstPage || hasNext()) {
      <nav aria-label="Pagination" class="flex items-center justify-between gap-4">
        <a
          ccButton
          variant="secondary"
          [disabled]="!hasPrevious()"
          [routerLink]="hasPrevious() ? [] : null"
          [queryParams]="params(page() - 1)"
          queryParamsHandling="merge"
          >Previous</a
        >
        <p class="text-sm text-ink-muted tabular-nums" aria-live="polite">Page {{ page() }}</p>
        <a
          ccButton
          variant="secondary"
          [disabled]="!hasNext()"
          [routerLink]="hasNext() ? [] : null"
          [queryParams]="params(page() + 1)"
          queryParamsHandling="merge"
          >Next</a
        >
      </nav>
    }
  `,
})
export class Pager {
  readonly page = input.required<number>();
  readonly hasNext = input.required<boolean>();
  readonly param = input('page');

  protected readonly firstPage = FIRST_PAGE;
  protected readonly hasPrevious = computed(() => this.page() > FIRST_PAGE);

  protected params(page: number): Record<string, string | null> {
    return { [this.param()]: page <= FIRST_PAGE ? null : String(page) };
  }
}

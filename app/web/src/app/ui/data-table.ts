import { NgTemplateOutlet } from '@angular/common';
import {
  Component,
  computed,
  contentChild,
  Directive,
  inject,
  input,
  output,
  TemplateRef,
} from '@angular/core';
import { LucideArrowDown, LucideArrowUp, LucideArrowUpDown } from '@lucide/angular';
import { Spinner } from './spinner';

export type SortDirection = 'asc' | 'desc';

export type TableSort = {
  field: string;
  direction: SortDirection;
};

export type TableColumn = {
  label: string;
  sortField?: string;
  sticky?: 'start' | 'end';
};

export type TableRowContext<T> = {
  $implicit: T;
};

export type TableRowItem = {
  id: string;
};

@Directive({ selector: 'ng-template[ccTableRow]' })
export class TableRow<T extends TableRowItem> {
  readonly rows = input.required<readonly T[]>({ alias: 'ccTableRow' });
  readonly template = inject<TemplateRef<TableRowContext<T>>>(TemplateRef);

  static ngTemplateContextGuard<T extends TableRowItem>(
    _directive: TableRow<T>,
    _context: unknown,
  ): _context is TableRowContext<T> {
    return true;
  }
}

@Component({
  selector: 'cc-data-table',
  imports: [NgTemplateOutlet, LucideArrowDown, LucideArrowUp, LucideArrowUpDown, Spinner],
  host: { class: 'block' },
  templateUrl: './data-table.html',
})
export class DataTable {
  readonly caption = input.required<string>();
  readonly columns = input.required<readonly (string | TableColumn)[]>();
  readonly emptyText = input.required<string>();
  readonly loading = input(false);
  readonly sort = input<TableSort | null>(null);
  readonly sortChange = output<TableSort>();

  private readonly row = contentChild<TableRow<TableRowItem>>(TableRow);

  protected readonly headers = computed<TableColumn[]>(() =>
    this.columns().map((column, index) =>
      typeof column === 'string'
        ? { label: column, sticky: index === 0 ? 'start' : undefined }
        : column,
    ),
  );
  protected readonly rows = computed(() => this.row()?.rows() ?? []);
  protected readonly template = computed(() => this.row()?.template ?? null);

  protected headerClass(column: TableColumn): string {
    if (column.sticky === 'start') {
      return 'sticky left-0 z-10 bg-raised';
    }
    if (column.sticky === 'end') {
      return 'bg-raised text-right sm:sticky sm:right-0 sm:z-10';
    }
    return '';
  }

  protected ariaSort(column: TableColumn): 'ascending' | 'descending' | 'none' | null {
    if (column.sortField === undefined) {
      return null;
    }
    const sort = this.sort();
    if (sort?.field !== column.sortField) {
      return 'none';
    }
    return sort.direction === 'asc' ? 'ascending' : 'descending';
  }

  protected toggleSort(field: string): void {
    const sort = this.sort();
    const direction: SortDirection =
      sort?.field === field && sort.direction === 'asc' ? 'desc' : 'asc';
    this.sortChange.emit({ field, direction });
  }
}

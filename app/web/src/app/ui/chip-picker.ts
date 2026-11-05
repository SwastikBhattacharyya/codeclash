import {
  Component,
  computed,
  inject,
  input,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import type { FormValueControl, ValidationError } from '@angular/forms/signals';
import { LucideCheck, LucideX } from '@lucide/angular';
import { Button } from './button';
import { FieldIds } from './field-ids';
import { TextField } from './text-field';

export type ChipOption = {
  id: string;
  name: string;
};

const UNKNOWN_LABEL = 'Unknown';
const LOADING_LABEL = 'Loading…';

export type ChipPickerStatus = 'loading' | 'error' | 'ready';

@Component({
  selector: 'cc-chip-picker',
  imports: [LucideCheck, LucideX, Button, TextField],
  host: { class: 'block', '(keydown.enter)': 'onEnter($event)' },
  templateUrl: './chip-picker.html',
})
export class ChipPicker implements FormValueControl<string[]> {
  readonly value = model<string[]>([]);
  readonly label = input.required<string>();
  readonly options = input.required<readonly ChipOption[]>();
  readonly emptyText = input('Nothing to choose from yet');
  readonly prefix = input('');
  readonly status = input<ChipPickerStatus>('ready');
  readonly errorText = input('These options couldn’t be loaded.');
  readonly retry = output<void>();
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly invalid = input(false);
  readonly touched = input(false);
  readonly disabled = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly filterField = viewChild.required(TextField);

  protected readonly id = inject(FieldIds).create('chips');
  protected readonly labelId = `${this.id}-label`;
  protected readonly errorId = `${this.id}-error`;
  protected readonly filter = signal('');
  protected readonly selectedSet = computed(() => new Set(this.value()));
  protected readonly chips = computed<ChipOption[]>(() => {
    const names = new Map(this.options().map((option) => [option.id, option.name]));
    const fallback = this.status() === 'loading' ? LOADING_LABEL : UNKNOWN_LABEL;
    return this.value().map((id) => ({ id, name: names.get(id) ?? fallback }));
  });
  protected readonly visible = computed(() => {
    const term = this.filter().trim().toLowerCase();
    return term === ''
      ? this.options()
      : this.options().filter((option) => option.name.toLowerCase().includes(term));
  });
  protected readonly showError = computed(() => this.touched() && this.invalid());
  protected readonly errorMessage = computed(() =>
    this.showError() ? (this.errors()[0]?.message ?? 'Check this value') : '',
  );

  protected toggle(option: ChipOption, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.set(option.id, checked);
    this.touch.emit();
  }

  protected remove(chip: ChipOption): void {
    this.set(chip.id, false);
    this.touch.emit();
    this.filterField().focus();
  }

  protected onEnter(event: Event): void {
    if (event.target instanceof HTMLInputElement) {
      event.preventDefault();
    }
  }

  private set(id: string, selected: boolean): void {
    const current = this.value();
    if (selected && !current.includes(id)) {
      this.value.set([...current, id]);
    }
    if (!selected) {
      this.value.set(current.filter((candidate) => candidate !== id));
    }
  }
}

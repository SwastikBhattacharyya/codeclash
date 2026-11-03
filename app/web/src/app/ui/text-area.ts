import {
  Component,
  computed,
  type ElementRef,
  inject,
  input,
  model,
  output,
  viewChild,
} from '@angular/core';
import type { FormValueControl, ValidationError } from '@angular/forms/signals';
import { FieldIds } from './field-ids';

@Component({
  selector: 'cc-text-area',
  host: { class: 'block' },
  templateUrl: './text-area.html',
})
export class TextArea implements FormValueControl<string> {
  readonly value = model('');
  readonly label = input.required<string>();
  readonly hint = input('');
  readonly rows = input(4);
  readonly limit = input<number | null>(null);
  readonly placeholder = input('');
  readonly spellcheck = input(true);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly invalid = input(false);
  readonly touched = input(false);
  readonly disabled = input(false);
  readonly readonly = input(false);
  readonly required = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly control = viewChild.required<ElementRef<HTMLTextAreaElement>>('control');

  protected readonly id = inject(FieldIds).create('text-area');
  protected readonly hintId = `${this.id}-hint`;
  protected readonly counterId = `${this.id}-counter`;
  protected readonly errorId = `${this.id}-error`;
  protected readonly length = computed(() => [...this.value().trim()].length);
  protected readonly overLimit = computed(() => {
    const limit = this.limit();
    return limit !== null && this.length() > limit;
  });
  protected readonly showError = computed(() => this.touched() && this.invalid());
  protected readonly errorMessage = computed(() =>
    this.showError() ? (this.errors()[0]?.message ?? 'Check this value') : '',
  );
  protected readonly describedBy = computed(() => {
    const ids: string[] = [];
    if (this.hint()) {
      ids.push(this.hintId);
    }
    if (this.limit() !== null) {
      ids.push(this.counterId);
    }
    if (this.showError()) {
      ids.push(this.errorId);
    }
    return ids.length > 0 ? ids.join(' ') : null;
  });

  focus(options?: FocusOptions): void {
    this.control().nativeElement.focus(options);
  }

  protected onInput(event: Event): void {
    this.value.set((event.target as HTMLTextAreaElement).value);
  }
}

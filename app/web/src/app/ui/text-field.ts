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

export type TextFieldType = 'text' | 'email';

export type TextFieldInputMode = 'text' | 'email' | 'numeric';

@Component({
  selector: 'cc-text-field',
  host: { class: 'block' },
  templateUrl: './text-field.html',
})
export class TextField implements FormValueControl<string> {
  readonly value = model('');
  readonly label = input.required<string>();
  readonly hint = input('');
  readonly type = input<TextFieldType>('text');
  readonly autocomplete = input('off');
  readonly inputmode = input<TextFieldInputMode | null>(null);
  readonly placeholder = input('');
  readonly autocapitalize = input<'off' | 'none' | 'sentences' | 'words'>('sentences');
  readonly spellcheck = input(true);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly invalid = input(false);
  readonly touched = input(false);
  readonly disabled = input(false);
  readonly readonly = input(false);
  readonly required = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly control = viewChild.required<ElementRef<HTMLInputElement>>('control');

  protected readonly id = inject(FieldIds).create('text');
  protected readonly hintId = `${this.id}-hint`;
  protected readonly errorId = `${this.id}-error`;
  protected readonly showError = computed(() => this.touched() && this.invalid());
  protected readonly errorMessage = computed(() =>
    this.showError() ? (this.errors()[0]?.message ?? 'Check this value') : '',
  );
  protected readonly describedBy = computed(() => {
    const ids: string[] = [];
    if (this.hint()) {
      ids.push(this.hintId);
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
    this.value.set((event.target as HTMLInputElement).value);
  }
}

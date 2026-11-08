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
import { localTimeZone } from '../core/time/local-datetime';
import { FieldIds } from './field-ids';

const MINUTE_STEP_SECONDS = 60;

@Component({
  selector: 'cc-datetime-field',
  host: { class: 'block' },
  templateUrl: './datetime-field.html',
})
export class DateTimeField implements FormValueControl<string> {
  readonly value = model('');
  readonly label = input.required<string>();
  readonly hint = input('');
  readonly showZone = input(true);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly invalid = input(false);
  readonly touched = input(false);
  readonly disabled = input(false);
  readonly readonly = input(false);
  readonly required = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly control = viewChild.required<ElementRef<HTMLInputElement>>('control');

  protected readonly step = MINUTE_STEP_SECONDS;
  protected readonly id = inject(FieldIds).create('datetime');
  protected readonly hintId = `${this.id}-hint`;
  protected readonly errorId = `${this.id}-error`;
  protected readonly hintText = computed(() => {
    const parts = [this.hint()];
    if (this.showZone()) {
      parts.push(`Time zone: ${localTimeZone()}`);
    }
    return parts.filter((part) => part !== '').join(' · ');
  });
  protected readonly showError = computed(() => this.touched() && this.invalid());
  protected readonly errorMessage = computed(() =>
    this.showError() ? (this.errors()[0]?.message ?? 'Check this value') : '',
  );
  protected readonly describedBy = computed(() => {
    const ids: string[] = [];
    if (this.hintText()) {
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

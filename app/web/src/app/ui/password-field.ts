import {
  Component,
  computed,
  type ElementRef,
  inject,
  input,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import type { FormValueControl, ValidationError } from '@angular/forms/signals';
import { LucideCheck, LucideCircle, LucideEye, LucideEyeOff } from '@lucide/angular';
import { FieldIds } from './field-ids';

export type PasswordRule = {
  label: string;
  test: (value: string) => boolean;
};

export type PasswordAutocomplete = 'current-password' | 'new-password';

@Component({
  selector: 'cc-password-field',
  imports: [LucideCheck, LucideCircle, LucideEye, LucideEyeOff],
  host: { class: 'block' },
  templateUrl: './password-field.html',
})
export class PasswordField implements FormValueControl<string> {
  readonly value = model('');
  readonly label = input.required<string>();
  readonly hint = input('');
  readonly autocomplete = input<PasswordAutocomplete>('current-password');
  readonly rules = input<readonly PasswordRule[]>([]);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly invalid = input(false);
  readonly touched = input(false);
  readonly disabled = input(false);
  readonly readonly = input(false);
  readonly required = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly control = viewChild.required<ElementRef<HTMLInputElement>>('control');

  protected readonly revealed = signal(false);
  protected readonly id = inject(FieldIds).create('password');
  protected readonly hintId = `${this.id}-hint`;
  protected readonly errorId = `${this.id}-error`;
  protected readonly rulesId = `${this.id}-rules`;
  protected readonly showError = computed(() => this.touched() && this.invalid());
  protected readonly errorMessage = computed(() =>
    this.showError() ? (this.errors()[0]?.message ?? 'Check this value') : '',
  );
  protected readonly ruleStates = computed(() =>
    this.rules().map((rule) => ({ label: rule.label, met: rule.test(this.value()) })),
  );
  protected readonly describedBy = computed(() => {
    const ids: string[] = [];
    if (this.hint()) {
      ids.push(this.hintId);
    }
    if (this.rules().length > 0) {
      ids.push(this.rulesId);
    }
    if (this.showError()) {
      ids.push(this.errorId);
    }
    return ids.length > 0 ? ids.join(' ') : null;
  });

  focus(options?: FocusOptions): void {
    this.control().nativeElement.focus(options);
  }

  reset(): void {
    this.revealed.set(false);
  }

  protected onInput(event: Event): void {
    this.value.set((event.target as HTMLInputElement).value);
  }

  protected toggleReveal(): void {
    this.revealed.update((revealed) => !revealed);
  }
}

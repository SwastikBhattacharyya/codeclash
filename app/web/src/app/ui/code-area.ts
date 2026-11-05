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

const HIGH_SURROGATE_START = 0xd800;
const HIGH_SURROGATE_END = 0xdbff;

@Component({
  selector: 'cc-code-area',
  host: { class: 'block' },
  templateUrl: './code-area.html',
})
export class CodeArea implements FormValueControl<string> {
  readonly value = model('');
  readonly label = input.required<string>();
  readonly hint = input('');
  readonly rows = input(8);
  readonly limit = input<number | null>(null);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly invalid = input(false);
  readonly touched = input(false);
  readonly disabled = input(false);
  readonly readonly = input(false);
  readonly required = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly control = viewChild.required<ElementRef<HTMLTextAreaElement>>('control');

  protected readonly id = inject(FieldIds).create('code');
  protected readonly hintId = `${this.id}-hint`;
  protected readonly counterId = `${this.id}-counter`;
  protected readonly errorId = `${this.id}-error`;
  protected readonly length = computed(() => CodeArea.codePoints(this.value()));
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

  static codePoints(value: string): number {
    let count = value.length;
    for (let index = 0; index < value.length; index += 1) {
      const code = value.charCodeAt(index);
      if (code >= HIGH_SURROGATE_START && code <= HIGH_SURROGATE_END) {
        count -= 1;
        index += 1;
      }
    }
    return count;
  }
}

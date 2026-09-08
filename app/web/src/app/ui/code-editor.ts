import {
  afterNextRender,
  Component,
  DestroyRef,
  DOCUMENT,
  type ElementRef,
  effect,
  inject,
  input,
  model,
  output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import type { FormValueControl } from '@angular/forms/signals';
import type { EditorHandle, EditorSelection } from './code-editor-setup';

export type EditorLanguage = 'cpp' | 'java' | 'python';

@Component({
  selector: 'cc-code-editor',
  host: { class: 'block' },
  template: `
    <div
      class="h-full overflow-hidden rounded-control border border-control bg-raised font-mono focus-within:border-ink"
    >
      <div #host class="h-full" [class.hidden]="!ready()"></div>
      @if (!ready()) {
        <textarea
          #fallback
          class="block h-full min-h-80 w-full resize-none bg-raised p-3 font-mono text-sm text-ink outline-none"
          wrap="off"
          spellcheck="false"
          autocapitalize="off"
          [attr.aria-label]="label()"
          [readOnly]="readonly() || disabled()"
          [value]="value()"
          (input)="onFallbackInput($event)"
          (keydown)="onFallbackKey($event)"
          (blur)="touch.emit()"
        ></textarea>
      }
    </div>
  `,
})
export class CodeEditor implements FormValueControl<string> {
  readonly value = model('');
  readonly language = input.required<EditorLanguage>();
  readonly label = input.required<string>();
  readonly readonly = input(false);
  readonly disabled = input(false);
  readonly touch = output<void>();
  readonly submit = output<void>();

  private readonly host = viewChild.required<ElementRef<HTMLElement>>('host');
  private readonly fallback = viewChild<ElementRef<HTMLTextAreaElement>>('fallback');
  private readonly document = inject(DOCUMENT);
  private handle: EditorHandle | null = null;
  private appliedLanguage: EditorLanguage | null = null;
  private destroyed = false;

  protected readonly ready = signal(false);

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.destroyed = true;
      this.handle?.destroy();
      this.handle = null;
    });
    afterNextRender(() => void this.mount());
    effect(() => {
      const text = this.value();
      const language = this.language();
      if (this.ready()) {
        untracked(() => this.apply(text, language));
      }
    });
    effect(() => {
      const locked = this.readonly() || this.disabled();
      if (this.ready()) {
        untracked(() => this.handle?.setReadOnly(locked));
      }
    });
    effect(() => {
      const label = this.label();
      if (this.ready()) {
        untracked(() => this.handle?.setLabel(label));
      }
    });
  }

  focus(): void {
    this.handle?.focus();
  }

  protected onFallbackInput(event: Event): void {
    this.value.set((event.target as HTMLTextAreaElement).value);
  }

  protected onFallbackKey(event: KeyboardEvent): void {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault();
      this.submit.emit();
    }
  }

  private apply(text: string, language: EditorLanguage): void {
    if (this.handle === null) {
      return;
    }
    if (language !== this.appliedLanguage) {
      this.appliedLanguage = language;
      this.handle.setDocument(text, language);
      return;
    }
    this.handle.setText(text);
  }

  private async mount(): Promise<void> {
    let setup: typeof import('./code-editor-setup');
    try {
      setup = await import('./code-editor-setup');
    } catch {
      return;
    }
    if (this.destroyed) {
      return;
    }
    this.appliedLanguage = this.language();
    this.handle = setup.createEditor(this.host().nativeElement, {
      doc: this.value(),
      selection: this.fallbackSelection(),
      language: this.language(),
      readOnly: this.readonly() || this.disabled(),
      label: this.label(),
      onChange: (text) => this.value.set(text),
      onSubmit: () => this.submit.emit(),
      onBlur: () => this.touch.emit(),
    });
    this.ready.set(true);
  }

  private fallbackSelection(): EditorSelection | null {
    const textarea = this.fallback()?.nativeElement;
    if (!textarea || this.document.activeElement !== textarea) {
      return null;
    }
    return textarea.selectionDirection === 'backward'
      ? { anchor: textarea.selectionEnd, head: textarea.selectionStart }
      : { anchor: textarea.selectionStart, head: textarea.selectionEnd };
  }
}

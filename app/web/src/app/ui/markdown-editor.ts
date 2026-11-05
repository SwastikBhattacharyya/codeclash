import {
  afterNextRender,
  Component,
  computed,
  DOCUMENT,
  type ElementRef,
  Injector,
  inject,
  input,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import type { FormValueControl, ValidationError } from '@angular/forms/signals';
import { CodeArea } from './code-area';
import { FieldIds } from './field-ids';
import { Markdown } from './markdown';

type EditorTab = 'write' | 'preview';

@Component({
  selector: 'cc-markdown-editor',
  imports: [Markdown],
  host: { class: 'block' },
  templateUrl: './markdown-editor.html',
})
export class MarkdownEditor implements FormValueControl<string> {
  readonly value = model('');
  readonly label = input.required<string>();
  readonly hint = input('');
  readonly rows = input(16);
  readonly limit = input<number | null>(null);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly invalid = input(false);
  readonly touched = input(false);
  readonly disabled = input(false);
  readonly readonly = input(false);
  readonly required = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly control = viewChild<ElementRef<HTMLTextAreaElement>>('control');
  private readonly document = inject(DOCUMENT);
  private readonly injector = inject(Injector);

  protected readonly id = inject(FieldIds).create('markdown');
  protected readonly labelId = `${this.id}-label`;
  protected readonly writeTabId = `${this.id}-write-tab`;
  protected readonly previewTabId = `${this.id}-preview-tab`;
  protected readonly writePanelId = `${this.id}-write`;
  protected readonly previewPanelId = `${this.id}-preview`;
  protected readonly hintId = `${this.id}-hint`;
  protected readonly counterId = `${this.id}-counter`;
  protected readonly errorId = `${this.id}-error`;
  protected readonly tab = signal<EditorTab>('write');
  protected readonly length = computed(() => CodeArea.codePoints(this.value().trim()));
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
    this.tab.set('write');
    afterNextRender(() => this.control()?.nativeElement.focus(options), {
      injector: this.injector,
    });
  }

  protected select(tab: EditorTab): void {
    this.tab.set(tab);
  }

  protected onTabKey(event: KeyboardEvent): void {
    if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') {
      return;
    }
    event.preventDefault();
    const next: EditorTab = this.tab() === 'write' ? 'preview' : 'write';
    this.tab.set(next);
    const tabId = next === 'write' ? this.writeTabId : this.previewTabId;
    afterNextRender(() => this.document.getElementById(tabId)?.focus(), {
      injector: this.injector,
    });
  }

  protected onInput(event: Event): void {
    this.value.set((event.target as HTMLTextAreaElement).value);
  }
}

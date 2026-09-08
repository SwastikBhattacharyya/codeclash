import { isPlatformBrowser } from '@angular/common';
import {
  Component,
  computed,
  DestroyRef,
  DOCUMENT,
  effect,
  inject,
  input,
  PLATFORM_ID,
  signal,
} from '@angular/core';
import { LucideCopy } from '@lucide/angular';
import type { HLJSApi } from 'highlight.js';

const COPIED_LABEL = 'Copied';
const COPY_FAILED_LABEL = "Couldn't copy";
const COPY_STATUS_MS = 2000;

let highlighter: Promise<HLJSApi> | null = null;

function loadHighlighter(): Promise<HLJSApi> {
  highlighter ??= Promise.all([
    import('highlight.js/lib/core'),
    import('highlight.js/lib/languages/cpp'),
    import('highlight.js/lib/languages/java'),
    import('highlight.js/lib/languages/python'),
  ]).then(([core, cpp, java, python]) => {
    const hljs = core.default;
    hljs.registerLanguage('cpp', cpp.default);
    hljs.registerLanguage('java', java.default);
    hljs.registerLanguage('python', python.default);
    return hljs;
  });
  return highlighter;
}

@Component({
  selector: 'cc-code-view',
  imports: [LucideCopy],
  host: { class: 'block' },
  template: `
    <div class="overflow-hidden rounded-control border border-line bg-surface">
      <div class="flex items-center justify-between gap-3 border-b border-line px-3 py-1.5">
        <span class="text-xs font-medium text-ink-muted">{{ label() }}</span>
        <button
          type="button"
          class="inline-flex min-h-9 cursor-pointer items-center gap-1.5 rounded-control px-2 text-sm text-ink-muted transition-colors hover:bg-ink/6 hover:text-ink"
          (click)="copy()"
        >
          <svg lucideCopy class="size-4"></svg>
          {{ copyStatus() === '' ? 'Copy' : copyStatus() }}
        </button>
      </div>
      <div
        class="cc-code max-h-[60vh] overflow-auto font-mono text-sm leading-6"
        tabindex="0"
        role="region"
        [attr.aria-label]="label() + ' code'"
      >
        <div class="flex w-max min-w-full">
          <pre
            aria-hidden="true"
            class="sticky left-0 shrink-0 border-r border-line bg-surface px-3 py-3 text-right text-ink-muted select-none"
            >{{ gutter() }}</pre
          >
          @if (html(); as highlighted) {
            <pre class="flex-1 px-4 py-3 text-ink"><code [innerHTML]="highlighted"></code></pre>
          } @else {
            <pre class="flex-1 px-4 py-3 text-ink"><code>{{ code() }}</code></pre>
          }
        </div>
      </div>
      <span class="sr-only" aria-live="polite">{{ copyStatus() }}</span>
    </div>
  `,
})
export class CodeView {
  private readonly document = inject(DOCUMENT);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  readonly code = input.required<string>();
  readonly language = input.required<string>();
  readonly label = input('Code');

  private copyTimer: ReturnType<typeof setTimeout> | null = null;

  protected readonly html = signal<string | null>(null);
  protected readonly copyStatus = signal('');
  protected readonly gutter = computed(() => {
    const code = this.code().replace(/\n$/, '');
    const lines = code === '' ? 1 : code.split('\n').length;
    return Array.from({ length: lines }, (_, index) => index + 1).join('\n');
  });

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      if (this.copyTimer !== null) {
        clearTimeout(this.copyTimer);
      }
    });
    effect((onCleanup) => {
      const code = this.code();
      const language = this.language();
      this.html.set(null);
      if (!this.isBrowser) {
        return;
      }
      let active = true;
      onCleanup(() => {
        active = false;
      });
      void loadHighlighter().then((hljs) => {
        if (!active || hljs.getLanguage(language) === undefined) {
          return;
        }
        this.html.set(hljs.highlight(code, { language, ignoreIllegals: true }).value);
      });
    });
  }

  protected async copy(): Promise<void> {
    try {
      await this.document.defaultView?.navigator.clipboard.writeText(this.code());
      this.showCopyStatus(COPIED_LABEL);
    } catch {
      this.showCopyStatus(COPY_FAILED_LABEL);
    }
  }

  private showCopyStatus(status: string): void {
    this.copyStatus.set(status);
    if (this.copyTimer !== null) {
      clearTimeout(this.copyTimer);
    }
    this.copyTimer = setTimeout(() => {
      this.copyStatus.set('');
      this.copyTimer = null;
    }, COPY_STATUS_MS);
  }
}

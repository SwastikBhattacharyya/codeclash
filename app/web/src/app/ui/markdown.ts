import { Component, computed, inject, input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { renderMarkdown } from '../core/markdown/render-markdown';

@Component({
  selector: 'cc-markdown',
  host: { class: 'cc-prose block', '[innerHTML]': 'html()' },
  template: '',
})
export class Markdown {
  private readonly sanitizer = inject(DomSanitizer);

  readonly source = input.required<string>();

  protected readonly html = computed(() =>
    this.sanitizer.bypassSecurityTrustHtml(renderMarkdown(this.source())),
  );
}

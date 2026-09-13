import { katex } from '@mdit/plugin-katex';
import markdownIt, { type MarkdownIt } from 'markdown-it';

let renderer: MarkdownIt | null = null;

function markdownRenderer(): MarkdownIt {
  if (renderer === null) {
    renderer = markdownIt({ html: false, linkify: true, typographer: false });
    renderer.use(katex, {
      delimiters: 'dollars',
      mathFence: true,
      throwOnError: false,
      trust: false,
      logger: () => undefined,
    });
  }
  return renderer;
}

export function renderMarkdown(source: string): string {
  return markdownRenderer().render(source);
}

import { closeBrackets, closeBracketsKeymap } from '@codemirror/autocomplete';
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands';
import { cpp } from '@codemirror/lang-cpp';
import { java } from '@codemirror/lang-java';
import { python } from '@codemirror/lang-python';
import {
  bracketMatching,
  HighlightStyle,
  indentOnInput,
  indentUnit,
  syntaxHighlighting,
} from '@codemirror/language';
import { Compartment, EditorState, type Extension, Prec } from '@codemirror/state';
import {
  drawSelection,
  EditorView,
  highlightActiveLine,
  highlightActiveLineGutter,
  highlightSpecialChars,
  keymap,
  lineNumbers,
} from '@codemirror/view';
import { tags } from '@lezer/highlight';
import type { EditorLanguage } from './code-editor';

export type EditorSelection = {
  anchor: number;
  head: number;
};

export type EditorOptions = {
  doc: string;
  selection: EditorSelection | null;
  language: EditorLanguage;
  readOnly: boolean;
  label: string;
  onChange: (text: string) => void;
  onSubmit: () => void;
  onBlur: () => void;
};

export type EditorHandle = {
  setText(text: string): void;
  setDocument(text: string, language: EditorLanguage): void;
  setReadOnly(readOnly: boolean): void;
  setLabel(label: string): void;
  focus(): void;
  destroy(): void;
};

const INDENT = '    ';

const HIGHLIGHT = HighlightStyle.define([
  {
    tag: [tags.keyword, tags.controlKeyword, tags.modifier, tags.operatorKeyword, tags.self],
    color: 'var(--cc-code-keyword)',
  },
  { tag: [tags.string, tags.special(tags.string), tags.character], color: 'var(--cc-code-string)' },
  {
    tag: [tags.comment, tags.lineComment, tags.blockComment],
    color: 'var(--cc-code-comment)',
    fontStyle: 'italic',
  },
  { tag: [tags.number, tags.bool, tags.null, tags.atom], color: 'var(--cc-code-number)' },
  {
    tag: [
      tags.function(tags.variableName),
      tags.function(tags.propertyName),
      tags.definition(tags.function(tags.variableName)),
    ],
    color: 'var(--cc-code-title)',
  },
  {
    tag: [tags.typeName, tags.className, tags.standard(tags.typeName), tags.namespace],
    color: 'var(--cc-code-type)',
  },
  { tag: [tags.processingInstruction, tags.meta, tags.macroName], color: 'var(--cc-code-meta)' },
]);

const THEME = EditorView.theme({
  '&': {
    height: '100%',
    fontSize: '14px',
    backgroundColor: 'var(--cc-raised)',
    color: 'var(--cc-ink)',
  },
  '&.cm-focused': { outline: 'none' },
  '.cm-scroller': { fontFamily: 'inherit', lineHeight: '1.6', overflow: 'auto' },
  '.cm-content': { caretColor: 'var(--cc-ink)', paddingBlock: '0.75rem' },
  '.cm-gutters': {
    backgroundColor: 'var(--cc-surface)',
    color: 'var(--cc-ink-muted)',
    borderRight: '1px solid var(--cc-line)',
  },
  '.cm-activeLine': { backgroundColor: 'color-mix(in srgb, var(--cc-ink) 5%, transparent)' },
  '.cm-activeLineGutter': {
    backgroundColor: 'color-mix(in srgb, var(--cc-ink) 8%, transparent)',
    color: 'var(--cc-ink)',
  },
  '.cm-cursor, .cm-dropCursor': { borderLeftColor: 'var(--cc-ink)' },
  '&.cm-focused > .cm-scroller > .cm-selectionLayer .cm-selectionBackground, .cm-selectionBackground, .cm-content ::selection':
    { backgroundColor: 'color-mix(in srgb, var(--cc-accent) 35%, transparent)' },
  '.cm-matchingBracket': {
    backgroundColor: 'color-mix(in srgb, var(--cc-accent) 30%, transparent)',
    outline: '1px solid color-mix(in srgb, var(--cc-accent) 70%, transparent)',
  },
});

function languageSupport(language: EditorLanguage): Extension {
  switch (language) {
    case 'cpp':
      return [cpp(), indentUnit.of(INDENT)];
    case 'java':
      return [java(), indentUnit.of(INDENT)];
    case 'python':
      return [python(), indentUnit.of(INDENT)];
  }
}

function readOnlyState(readOnly: boolean): Extension {
  return [EditorState.readOnly.of(readOnly), EditorView.editable.of(!readOnly)];
}

function labelAttributes(label: string): Extension {
  return EditorView.contentAttributes.of({ 'aria-label': label });
}

export function createEditor(parent: HTMLElement, options: EditorOptions): EditorHandle {
  const readOnlySlot = new Compartment();
  const labelSlot = new Compartment();
  let readOnly = options.readOnly;
  let label = options.label;
  let applying = false;
  let lastEmitted: string | null = null;
  const createState = (doc: string, language: EditorLanguage): EditorState =>
    EditorState.create({
      doc,
      extensions: [
        lineNumbers(),
        highlightActiveLineGutter(),
        highlightSpecialChars(),
        history(),
        drawSelection(),
        indentOnInput(),
        bracketMatching(),
        closeBrackets(),
        highlightActiveLine(),
        syntaxHighlighting(HIGHLIGHT),
        THEME,
        Prec.highest(
          keymap.of([
            {
              key: 'Mod-Enter',
              run: () => {
                options.onSubmit();
                return true;
              },
            },
          ]),
        ),
        keymap.of([...closeBracketsKeymap, ...defaultKeymap, ...historyKeymap, indentWithTab]),
        languageSupport(language),
        readOnlySlot.of(readOnlyState(readOnly)),
        labelSlot.of(labelAttributes(label)),
        EditorView.updateListener.of((update) => {
          if (update.docChanged && !applying) {
            lastEmitted = update.state.doc.toString();
            options.onChange(lastEmitted);
          }
          if (update.focusChanged && !update.view.hasFocus) {
            options.onBlur();
          }
        }),
      ],
    });
  const view = new EditorView({ parent, state: createState(options.doc, options.language) });
  if (options.selection !== null) {
    const length = view.state.doc.length;
    view.dispatch({
      selection: {
        anchor: Math.min(options.selection.anchor, length),
        head: Math.min(options.selection.head, length),
      },
    });
    view.focus();
  }
  return {
    setText: (text) => {
      if (text === lastEmitted || text === view.state.doc.toString()) {
        return;
      }
      applying = true;
      try {
        view.dispatch({ changes: { from: 0, to: view.state.doc.length, insert: text } });
        lastEmitted = text;
      } finally {
        applying = false;
      }
    },
    setDocument: (text, language) => {
      lastEmitted = null;
      view.setState(createState(text, language));
    },
    setReadOnly: (next) => {
      readOnly = next;
      view.dispatch({ effects: readOnlySlot.reconfigure(readOnlyState(next)) });
    },
    setLabel: (next) => {
      label = next;
      view.dispatch({ effects: labelSlot.reconfigure(labelAttributes(next)) });
    },
    focus: () => view.focus(),
    destroy: () => view.destroy(),
  };
}

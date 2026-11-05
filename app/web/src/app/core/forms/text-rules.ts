import { type SchemaPath, validate } from '@angular/forms/signals';
import { codePointLength, requiredText } from '../auth/user-rules';

const LINE_BREAK_PATTERN = /[\n\r\u0085\u2028\u2029]/;

export function textRules(
  path: SchemaPath<string>,
  label: string,
  required: string,
  min: number,
  max: number,
): void {
  requiredText(path, required);
  validate(path, ({ value }) => {
    const text = value().trim();
    if (text === '') {
      return null;
    }
    const size = codePointLength(text);
    if (size < min) {
      return { kind: 'minLength', message: `${label} must contain at least ${min} characters` };
    }
    if (size > max) {
      return { kind: 'maxLength', message: `${label} must contain at most ${max} characters` };
    }
    return null;
  });
}

export function singleLineRules(path: SchemaPath<string>, label: string): void {
  validate(path, ({ value }) =>
    LINE_BREAK_PATTERN.test(value().trim())
      ? { kind: 'pattern', message: `${label} must fit on a single line` }
      : null,
  );
}

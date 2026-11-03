import { type SchemaPath, validate } from '@angular/forms/signals';
import { codePointLength, requiredText } from '../../../core/auth/user-rules';

export const LOOKUP_NAME_MIN_SIZE = 2;
export const LOOKUP_NAME_MAX_SIZE = 64;
export const LOOKUP_DESCRIPTION_MAX_SIZE = 255;

const LINE_BREAK_PATTERN = /[\n\r\u0085\u2028\u2029]/;

export function lookupNameRules(path: SchemaPath<string>): void {
  requiredText(path, 'Enter a name');
  validate(path, ({ value }) => {
    const name = value().trim();
    if (name === '') {
      return null;
    }
    const size = codePointLength(name);
    if (size < LOOKUP_NAME_MIN_SIZE) {
      return {
        kind: 'minLength',
        message: `Name must contain at least ${LOOKUP_NAME_MIN_SIZE} characters`,
      };
    }
    if (size > LOOKUP_NAME_MAX_SIZE) {
      return {
        kind: 'maxLength',
        message: `Name must contain at most ${LOOKUP_NAME_MAX_SIZE} characters`,
      };
    }
    if (LINE_BREAK_PATTERN.test(name)) {
      return { kind: 'pattern', message: 'Name must fit on a single line' };
    }
    return null;
  });
}

export function lookupDescriptionRules(path: SchemaPath<string>): void {
  requiredText(path, 'Enter a description');
  validate(path, ({ value }) => {
    const description = value().trim();
    if (description === '') {
      return null;
    }
    if (codePointLength(description) > LOOKUP_DESCRIPTION_MAX_SIZE) {
      return {
        kind: 'maxLength',
        message: `Description must contain at most ${LOOKUP_DESCRIPTION_MAX_SIZE} characters`,
      };
    }
    return null;
  });
}

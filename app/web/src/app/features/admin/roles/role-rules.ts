import { type SchemaPath, validate } from '@angular/forms/signals';
import { codePointLength, requiredText } from '../../../core/auth/user-rules';

export const ROLE_NAME_MAX_SIZE = 255;
export const ROLE_DESCRIPTION_MAX_SIZE = 255;
export const ROLE_NAME_PATTERN = /^[a-z]+(?:_[a-z]+)*$/;
export const ROLE_NAME_HINT = 'Lowercase letters and underscores, e.g. problem_setter';

export function roleNameRules(path: SchemaPath<string>): void {
  requiredText(path, 'Enter a name');
  validate(path, ({ value }) => {
    const name = value().trim();
    if (name === '') {
      return null;
    }
    if (codePointLength(name) > ROLE_NAME_MAX_SIZE) {
      return {
        kind: 'maxLength',
        message: `Name must contain at most ${ROLE_NAME_MAX_SIZE} characters`,
      };
    }
    if (!ROLE_NAME_PATTERN.test(name)) {
      return {
        kind: 'pattern',
        message: 'Use lowercase letters, with single underscores between words',
      };
    }
    return null;
  });
}

export function roleDescriptionRules(path: SchemaPath<string>): void {
  requiredText(path, 'Enter a description');
  validate(path, ({ value }) =>
    codePointLength(value().trim()) > ROLE_DESCRIPTION_MAX_SIZE
      ? {
          kind: 'maxLength',
          message: `Description must contain at most ${ROLE_DESCRIPTION_MAX_SIZE} characters`,
        }
      : null,
  );
}

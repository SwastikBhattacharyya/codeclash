import { email, required, type SchemaPath, validate } from '@angular/forms/signals';
import type { PasswordRule } from '../../ui/password-field';

export const NAME_MIN_LENGTH = 2;
export const NAME_MAX_LENGTH = 255;
export const NAME_MIN_LETTERS = 2;
export const USERNAME_MIN_LENGTH = 3;
export const USERNAME_MAX_LENGTH = 32;
export const USERNAME_PATTERN = /^[A-Za-z0-9_-]+$/;
export const EMAIL_MAX_LENGTH = 254;
export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_MAX_LENGTH = 256;

const ASCII_LETTER_PATTERN = /[A-Za-z]/g;
const LOWERCASE_PATTERN = /\p{Ll}/u;
const UPPERCASE_PATTERN = /\p{Lu}/u;
const DIGIT_PATTERN = /\p{Nd}/u;
const SPECIAL_PATTERN = /[^\p{L}\p{Nd}\s\p{Z}\p{Cc}\p{Cf}]/u;

export function normalizedPassword(value: string): string {
  return value.normalize('NFKC');
}

export function codePointLength(value: string): number {
  return [...value].length;
}

export const PASSWORD_RULES: readonly PasswordRule[] = [
  {
    label: `At least ${PASSWORD_MIN_LENGTH} characters`,
    test: (value) => codePointLength(normalizedPassword(value)) >= PASSWORD_MIN_LENGTH,
  },
  {
    label: 'A lowercase letter',
    test: (value) => LOWERCASE_PATTERN.test(normalizedPassword(value)),
  },
  {
    label: 'An uppercase letter',
    test: (value) => UPPERCASE_PATTERN.test(normalizedPassword(value)),
  },
  { label: 'A number', test: (value) => DIGIT_PATTERN.test(normalizedPassword(value)) },
  {
    label: 'A special character',
    test: (value) => SPECIAL_PATTERN.test(normalizedPassword(value)),
  },
];

export function requiredText(path: SchemaPath<string>, message: string): void {
  required(path, { message });
  validate(path, ({ value }) =>
    value() !== '' && value().trim() === '' ? { kind: 'required', message } : null,
  );
}

export function nameRules(path: SchemaPath<string>): void {
  requiredText(path, 'Enter your name');
  validate(path, ({ value }) => {
    const name = value().trim();
    if (name === '') {
      return null;
    }
    const length = codePointLength(name);
    if (length < NAME_MIN_LENGTH) {
      return {
        kind: 'minLength',
        message: `Name must contain at least ${NAME_MIN_LENGTH} characters`,
      };
    }
    if (length > NAME_MAX_LENGTH) {
      return {
        kind: 'maxLength',
        message: `Name must contain at most ${NAME_MAX_LENGTH} characters`,
      };
    }
    if ((name.match(ASCII_LETTER_PATTERN) ?? []).length < NAME_MIN_LETTERS) {
      return {
        kind: 'minLetters',
        message: `Name must contain at least ${NAME_MIN_LETTERS} letters`,
      };
    }
    return null;
  });
}

export function usernameRules(path: SchemaPath<string>): void {
  requiredText(path, 'Choose a username');
  validate(path, ({ value }) => {
    const username = value().trim();
    if (username === '') {
      return null;
    }
    if (username.length < USERNAME_MIN_LENGTH) {
      return {
        kind: 'minLength',
        message: `Username must contain at least ${USERNAME_MIN_LENGTH} characters`,
      };
    }
    if (username.length > USERNAME_MAX_LENGTH) {
      return {
        kind: 'maxLength',
        message: `Username must contain at most ${USERNAME_MAX_LENGTH} characters`,
      };
    }
    if (!USERNAME_PATTERN.test(username)) {
      return { kind: 'pattern', message: 'Username may only contain letters, numbers, _ and -' };
    }
    return null;
  });
}

export function emailRules(path: SchemaPath<string>): void {
  requiredText(path, 'Enter your email');
  email(path, { message: 'Enter a valid email address' });
  validate(path, ({ value }) =>
    codePointLength(value().trim()) > EMAIL_MAX_LENGTH
      ? { kind: 'maxLength', message: `Email must contain at most ${EMAIL_MAX_LENGTH} characters` }
      : null,
  );
}

export function passwordRules(path: SchemaPath<string>): void {
  required(path, { message: 'Enter a password' });
  validate(path, ({ value }) => {
    const password = value();
    if (password === '') {
      return null;
    }
    if (codePointLength(normalizedPassword(password)) > PASSWORD_MAX_LENGTH) {
      return {
        kind: 'maxLength',
        message: `Password must contain at most ${PASSWORD_MAX_LENGTH} characters`,
      };
    }
    const unmet = PASSWORD_RULES.find((rule) => !rule.test(password));
    return unmet
      ? { kind: 'passwordRule', message: `Password needs: ${unmet.label.toLowerCase()}` }
      : null;
  });
}

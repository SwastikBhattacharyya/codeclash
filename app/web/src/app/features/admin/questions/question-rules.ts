import { type SchemaPath, validate } from '@angular/forms/signals';
import { requiredText } from '../../../core/auth/user-rules';
import { singleLineRules, textRules } from '../../../core/forms/text-rules';
import { CodeArea } from '../../../ui/code-area';

export const QUESTION_TITLE_MIN_SIZE = 8;
export const QUESTION_TITLE_MAX_SIZE = 64;
export const QUESTION_SUMMARY_MIN_SIZE = 16;
export const QUESTION_SUMMARY_MAX_SIZE = 256;
export const QUESTION_DESCRIPTION_MIN_SIZE = 32;
export const QUESTION_DESCRIPTION_MAX_SIZE = 65535;
export const QUESTION_RUN_TIME_MIN_MS = 100;
export const QUESTION_RUN_TIME_MAX_MS = 10000;
export const QUESTION_RUN_TIME_DEFAULT_MS = 2000;
export const QUESTION_RAM_MIN_MB = 64;
export const QUESTION_RAM_MAX_MB = 1024;
export const QUESTION_RAM_DEFAULT_MB = 256;
export const TEST_CASE_DATA_MAX_SIZE = 1048576;

const DIGITS_PATTERN = /^\d+$/;

function rangeRules(
  path: SchemaPath<string>,
  label: string,
  unit: string,
  min: number,
  max: number,
): void {
  requiredText(path, `Enter the ${label.toLowerCase()}`);
  validate(path, ({ value }) => {
    const text = value().trim();
    if (text === '') {
      return null;
    }
    if (!DIGITS_PATTERN.test(text)) {
      return { kind: 'pattern', message: `${label} must be a whole number of ${unit}` };
    }
    const number = Number(text);
    if (number < min || number > max) {
      return {
        kind: 'range',
        message: `${label} must be between ${min} and ${max} ${unit}`,
      };
    }
    return null;
  });
}

export function questionTitleRules(path: SchemaPath<string>): void {
  textRules(path, 'Title', 'Enter a title', QUESTION_TITLE_MIN_SIZE, QUESTION_TITLE_MAX_SIZE);
  singleLineRules(path, 'Title');
}

export function questionSummaryRules(path: SchemaPath<string>): void {
  textRules(
    path,
    'Summary',
    'Enter a summary',
    QUESTION_SUMMARY_MIN_SIZE,
    QUESTION_SUMMARY_MAX_SIZE,
  );
}

export function questionDescriptionRules(path: SchemaPath<string>): void {
  textRules(
    path,
    'Description',
    'Enter a description',
    QUESTION_DESCRIPTION_MIN_SIZE,
    QUESTION_DESCRIPTION_MAX_SIZE,
  );
}

export function questionRunTimeRules(path: SchemaPath<string>): void {
  rangeRules(path, 'Time limit', 'ms', QUESTION_RUN_TIME_MIN_MS, QUESTION_RUN_TIME_MAX_MS);
}

export function questionRamRules(path: SchemaPath<string>): void {
  rangeRules(path, 'Memory limit', 'MB', QUESTION_RAM_MIN_MB, QUESTION_RAM_MAX_MB);
}

export function testCaseDataRules(path: SchemaPath<string>, label: string): void {
  validate(path, ({ value }) =>
    value().length > TEST_CASE_DATA_MAX_SIZE &&
    CodeArea.codePoints(value()) > TEST_CASE_DATA_MAX_SIZE
      ? {
          kind: 'maxLength',
          message: `${label} must contain at most ${TEST_CASE_DATA_MAX_SIZE} characters`,
        }
      : null,
  );
}

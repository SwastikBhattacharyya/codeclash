import { type SchemaPath, validate } from '@angular/forms/signals';
import { requiredText } from '../../../core/auth/user-rules';
import { singleLineRules, textRules } from '../../../core/forms/text-rules';
import { fromLocalInput } from '../../../core/time/local-datetime';

export const CONTEST_NAME_MIN_SIZE = 8;
export const CONTEST_NAME_MAX_SIZE = 64;
export const CONTEST_DESCRIPTION_MIN_SIZE = 32;
export const CONTEST_DESCRIPTION_MAX_SIZE = 65535;
export const CONTEST_POINTS_MIN = 1;
export const CONTEST_POINTS_MAX = 10000;
export const CONTEST_POINTS_DEFAULT = 100;

export type ScheduleChecks = {
  now: () => number;
  changed: (field: 'liveAt' | 'endAt') => boolean;
};

const DIGITS_PATTERN = /^\d+$/;

export function contestNameRules(path: SchemaPath<string>): void {
  textRules(path, 'Name', 'Enter a name', CONTEST_NAME_MIN_SIZE, CONTEST_NAME_MAX_SIZE);
  singleLineRules(path, 'Name');
}

export function contestDescriptionRules(path: SchemaPath<string>): void {
  textRules(
    path,
    'Description',
    'Enter a description',
    CONTEST_DESCRIPTION_MIN_SIZE,
    CONTEST_DESCRIPTION_MAX_SIZE,
  );
}

export function contestScheduleRules(
  liveAt: SchemaPath<string>,
  endAt: SchemaPath<string>,
  checks: ScheduleChecks,
): void {
  timeRules(liveAt, 'start', () => checks.changed('liveAt'), checks.now);
  timeRules(endAt, 'end', () => checks.changed('endAt'), checks.now);
  validate(endAt, (context) => {
    if (!checks.changed('liveAt') && !checks.changed('endAt')) {
      return null;
    }
    const end = fromLocalInput(context.value());
    const start = fromLocalInput(context.valueOf(liveAt));
    if (end === null || start === null) {
      return null;
    }
    return Date.parse(end) > Date.parse(start)
      ? null
      : { kind: 'order', message: 'End time must be after the start time' };
  });
}

export function contestPointsRules(path: SchemaPath<string>): void {
  requiredText(path, 'Enter the points');
  validate(path, ({ value }) => {
    const text = value().trim();
    if (text === '') {
      return null;
    }
    if (!DIGITS_PATTERN.test(text)) {
      return { kind: 'pattern', message: 'Points must be a whole number' };
    }
    const points = Number(text);
    if (points < CONTEST_POINTS_MIN || points > CONTEST_POINTS_MAX) {
      return {
        kind: 'range',
        message: `Points must be between ${CONTEST_POINTS_MIN} and ${CONTEST_POINTS_MAX}`,
      };
    }
    return null;
  });
}

function timeRules(
  path: SchemaPath<string>,
  label: 'start' | 'end',
  mustBeFuture: () => boolean,
  now: () => number,
): void {
  requiredText(path, `Choose the ${label} time`);
  validate(path, ({ value }) => {
    if (value().trim() === '') {
      return null;
    }
    const iso = fromLocalInput(value());
    if (iso === null) {
      return { kind: 'pattern', message: 'Enter a valid date and time' };
    }
    if (mustBeFuture() && Date.parse(iso) <= now()) {
      return {
        kind: 'future',
        message: `${label === 'start' ? 'Start' : 'End'} time must be in the future`,
      };
    }
    return null;
  });
}

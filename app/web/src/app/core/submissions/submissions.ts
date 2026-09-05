import { HttpClient, type HttpResourceRequest } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export type SubmissionState =
  | 'queued'
  | 'compiled'
  | 'compilation_error'
  | 'correct'
  | 'incorrect'
  | 'runtime_error'
  | 'time_limit_exceeded'
  | 'memory_limit_exceeded'
  | 'runner_error';

export type SubmissionLanguage = 'cpp' | 'java' | 'python';

export type Submission = {
  id: string;
  language: SubmissionLanguage;
  code: string;
  state: SubmissionState;
  failedTestCaseId: string | null;
  timeTakenMs: number | null;
  memoryUsedKb: number | null;
  compileOutput: string | null;
  actualOutput: string | null;
  questionId: string;
  username: string;
  createdAt: string;
  updatedAt: string;
};

export type NewSubmission = {
  questionId: string;
  language: SubmissionLanguage;
  code: string;
};

export type SubmissionHistoryQuery = {
  questionId: string;
  userId: string | null;
  createdAfter: string | null;
  createdBefore: string | null;
  page: number;
  size: number;
};

export type VerdictTone = 'success' | 'danger' | 'pending' | 'neutral';

export const SUBMISSIONS_URL = '/api/v1/submissions';
export const SUBMISSION_CODE_MAX_SIZE = 65_536;
export const SOLVED_LOOKUP_SIZE = 100;

export const SUBMISSION_STATES: readonly SubmissionState[] = [
  'queued',
  'compiled',
  'correct',
  'incorrect',
  'compilation_error',
  'runtime_error',
  'time_limit_exceeded',
  'memory_limit_exceeded',
  'runner_error',
];

export const SUBMISSION_LANGUAGES: readonly SubmissionLanguage[] = ['cpp', 'java', 'python'];

export const STATE_LABELS: Record<SubmissionState, string> = {
  queued: 'Queued',
  compiled: 'Compiled',
  compilation_error: 'Compilation error',
  correct: 'Accepted',
  incorrect: 'Wrong answer',
  runtime_error: 'Runtime error',
  time_limit_exceeded: 'Time limit exceeded',
  memory_limit_exceeded: 'Memory limit exceeded',
  runner_error: 'Runner error',
};

export const LANGUAGE_LABELS: Record<SubmissionLanguage, string> = {
  cpp: 'C++',
  java: 'Java',
  python: 'Python',
};

export const LANGUAGE_TIME_MULTIPLIERS: Record<SubmissionLanguage, number> = {
  cpp: 1,
  java: 2,
  python: 3,
};

const STATE_TONES: Record<SubmissionState, VerdictTone> = {
  queued: 'pending',
  compiled: 'pending',
  compilation_error: 'danger',
  correct: 'success',
  incorrect: 'danger',
  runtime_error: 'danger',
  time_limit_exceeded: 'danger',
  memory_limit_exceeded: 'danger',
  runner_error: 'neutral',
};

const PENDING_STATES: ReadonlySet<SubmissionState> = new Set(['queued', 'compiled']);
const FAILED_TEST_STATES: ReadonlySet<SubmissionState> = new Set([
  'incorrect',
  'runtime_error',
  'time_limit_exceeded',
  'memory_limit_exceeded',
]);
const KB_PER_MB = 1024;

export function verdictTone(state: SubmissionState): VerdictTone {
  return STATE_TONES[state];
}

export function isPending(state: SubmissionState): boolean {
  return PENDING_STATES.has(state);
}

const TRAILING_JAVA_WHITESPACE =
  /[\t\n\u000B\f\r\u001C-\u001F \u1680\u2000-\u2006\u2008-\u200A\u2028\u2029\u205F\u3000]+$/u;
const TRUNCATION_MARKER = '\n… output truncated\n';

function judgedLines(text: string): string[] {
  const lines = text.split('\n').map((line) => line.replace(TRAILING_JAVA_WHITESPACE, ''));
  while (lines.length > 0 && lines[lines.length - 1] === '') {
    lines.pop();
  }
  return lines;
}

export function firstDifferenceLine(expected: string, actual: string): number | null {
  const truncated = actual.endsWith(TRUNCATION_MARKER);
  const expectedLines = judgedLines(expected);
  const actualLines = judgedLines(truncated ? actual.slice(0, -TRUNCATION_MARKER.length) : actual);
  const length = truncated
    ? actualLines.length - 1
    : Math.max(expectedLines.length, actualLines.length);
  for (let index = 0; index < length; index++) {
    if (expectedLines[index] !== actualLines[index]) {
      return index + 1;
    }
  }
  return null;
}

export function failsOnTestCase(state: SubmissionState): boolean {
  return FAILED_TEST_STATES.has(state);
}

export function isSubmissionLanguage(value: unknown): value is SubmissionLanguage {
  return SUBMISSION_LANGUAGES.some((language) => language === value);
}

export function formatTime(ms: number | null): string {
  return ms === null ? '—' : `${ms} ms`;
}

export function formatMemory(kb: number | null): string {
  if (kb === null) {
    return '—';
  }
  return kb >= KB_PER_MB ? `${(kb / KB_PER_MB).toFixed(1)} MB` : `${kb} KB`;
}

export function submissionUrl(id: string): string {
  return `${SUBMISSIONS_URL}/${encodeURIComponent(id)}`;
}

export function submissionHistoryRequest(query: SubmissionHistoryQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    question_id: query.questionId,
    page: String(query.page),
    size: String(query.size),
    sort: 'created_at',
    direction: 'desc',
  };
  if (query.userId !== null) {
    params['user_id'] = query.userId;
  }
  if (query.createdAfter !== null) {
    params['created_at_0'] = query.createdAfter;
  }
  if (query.createdBefore !== null) {
    params['created_at_1'] = query.createdBefore;
  }
  return { url: SUBMISSIONS_URL, params };
}

export type SolvedWindow = {
  from: string;
  until: string;
};

export function solvedRequest(
  userId: string,
  questionIds: readonly string[],
  window: SolvedWindow | null = null,
): HttpResourceRequest {
  const params: Record<string, string | string[]> = {
    user_id: userId,
    state: 'correct',
    question_id: [...questionIds],
    size: String(SOLVED_LOOKUP_SIZE),
  };
  if (window !== null) {
    params['created_at_0'] = window.from;
    params['created_at_1'] = window.until;
  }
  return { url: SUBMISSIONS_URL, params };
}

@Service()
export class SubmissionsApi {
  private readonly http = inject(HttpClient);

  create(submission: NewSubmission): Promise<Submission> {
    return firstValueFrom(this.http.post<Submission>(SUBMISSIONS_URL, submission));
  }

  get(id: string): Promise<Submission> {
    return firstValueFrom(this.http.get<Submission>(submissionUrl(id)));
  }

  remove(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(submissionUrl(id)));
  }
}

import type { HttpResourceRequest } from '@angular/common/http';
import { LOOKUP_PAGE_SIZE } from '../../../core/api/page';
import { testCasesUrl } from '../../../core/problems/problems-api';
import {
  SUBMISSIONS_URL,
  type SubmissionLanguage,
  type SubmissionState,
} from '../../../core/submissions/submissions';
import type { SortDirection } from '../../../ui/data-table';

export type SubmissionSortField = 'state' | 'language' | 'created_at';

export type SubmissionQuery = {
  username: string;
  questionId: string | null;
  state: SubmissionState | null;
  language: SubmissionLanguage | null;
  page: number;
  sort: SubmissionSortField;
  direction: SortDirection;
};

export const SUBMISSION_ADMIN_PAGE_SIZE = 20;
export const SUBMISSION_FORBIDDEN_MESSAGE = "You don't have permission to delete this submission.";

export const SUBMISSION_SORT_FIELDS: readonly SubmissionSortField[] = [
  'state',
  'language',
  'created_at',
];

export function submissionsRequest(query: SubmissionQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    page: String(query.page),
    size: String(SUBMISSION_ADMIN_PAGE_SIZE),
    sort: query.sort,
    direction: query.direction,
  };
  if (query.username !== '') {
    params['username'] = query.username;
  }
  if (query.questionId !== null) {
    params['question_id'] = query.questionId;
  }
  if (query.state !== null) {
    params['state'] = query.state;
  }
  if (query.language !== null) {
    params['language'] = query.language;
  }
  return { url: SUBMISSIONS_URL, params };
}

export function questionTestCaseIdsRequest(questionId: string): HttpResourceRequest {
  return {
    url: testCasesUrl(questionId),
    params: { sort: 'id', direction: 'asc', size: String(LOOKUP_PAGE_SIZE) },
  };
}

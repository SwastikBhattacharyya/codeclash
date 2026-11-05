import { HttpClient, type HttpResourceRequest } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  QUESTIONS_URL,
  type Question,
  questionUrl,
  testCasesUrl,
  testCaseUrl,
} from '../../../core/problems/problems-api';
import { LOOKUP_PAGE_SIZE, type Page } from '../../../core/api/page';
import type { SortDirection } from '../../../ui/data-table';

export type AdminQuestion = Question;

export type QuestionStatus = 'published' | 'draft';

export type QuestionSortField = 'title' | 'visible' | 'updated_at';

export type QuestionQuery = {
  title: string;
  status: QuestionStatus | null;
  category: string | null;
  tag: string | null;
  page: number;
  sort: QuestionSortField;
  direction: SortDirection;
};

export type QuestionInput = {
  title: string;
  summary: string;
  description: string;
  isVisible: boolean;
  maxRunTimeMs: number;
  maxRamLimitMb: number;
  categories: string[];
  tags: string[];
};

export type QuestionChanges = Partial<QuestionInput>;

export type TestCase = {
  id: string;
  input: string;
  output: string;
  isPublic: boolean;
  questionId: string;
  createdAt: string;
  updatedAt: string;
};

export type TestCaseVisibility = 'public' | 'hidden';

export type TestCaseQuery = {
  visibility: TestCaseVisibility | null;
  page: number;
};

export type TestCaseInput = {
  input: string;
  output: string;
  isPublic: boolean;
};

export type TestCaseChanges = Partial<TestCaseInput>;

export const QUESTION_ADMIN_PAGE_SIZE = 20;
export const TEST_CASE_PAGE_SIZE = 20;
export const QUESTION_FORBIDDEN_MESSAGE = "You don't have permission to change this question.";
export const TEST_CASE_FORBIDDEN_MESSAGE = "You don't have permission to change test cases.";
export const QUESTION_STATUSES: readonly QuestionStatus[] = ['published', 'draft'];
export const QUESTION_SORT_FIELDS: readonly QuestionSortField[] = [
  'title',
  'visible',
  'updated_at',
];
export const TEST_CASE_VISIBILITIES: readonly TestCaseVisibility[] = ['public', 'hidden'];

export function questionsListRequest(query: QuestionQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    page: String(query.page),
    size: String(QUESTION_ADMIN_PAGE_SIZE),
    sort: query.sort,
    direction: query.direction,
  };
  if (query.title !== '') {
    params['title'] = query.title;
  }
  if (query.status !== null) {
    params['visible'] = String(query.status === 'published');
  }
  if (query.category !== null) {
    params['category'] = query.category;
  }
  if (query.tag !== null) {
    params['tag'] = query.tag;
  }
  return { url: QUESTIONS_URL, params };
}

export function testCasesRequest(questionId: string, query: TestCaseQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    page: String(query.page),
    size: String(TEST_CASE_PAGE_SIZE),
    sort: 'id',
    direction: 'asc',
  };
  if (query.visibility !== null) {
    params['is_public'] = String(query.visibility === 'public');
  }
  return { url: testCasesUrl(questionId), params };
}

export function hiddenTestCaseCountRequest(questionId: string): HttpResourceRequest {
  return { url: testCasesUrl(questionId), params: { is_public: 'false', size: '1' } };
}

export function normalizeLineEndings(value: string): string {
  return value.replace(/\r\n?/g, '\n');
}

@Service()
export class QuestionsApi {
  private readonly http = inject(HttpClient);

  create(input: QuestionInput): Promise<AdminQuestion> {
    return firstValueFrom(this.http.post<AdminQuestion>(QUESTIONS_URL, input));
  }

  update(id: string, changes: QuestionChanges): Promise<AdminQuestion> {
    return firstValueFrom(this.http.patch<AdminQuestion>(questionUrl(id), changes));
  }

  remove(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(questionUrl(id)));
  }

  createTestCase(questionId: string, input: TestCaseInput): Promise<TestCase> {
    return firstValueFrom(this.http.post<TestCase>(testCasesUrl(questionId), input));
  }

  updateTestCase(questionId: string, id: string, changes: TestCaseChanges): Promise<TestCase> {
    return firstValueFrom(this.http.patch<TestCase>(testCaseUrl(questionId, id), changes));
  }

  async hiddenTestCaseIds(questionId: string): Promise<string[]> {
    const ids: string[] = [];
    for (let page = 1; ; page++) {
      const result = await firstValueFrom(
        this.http.get<Page<TestCase>>(testCasesUrl(questionId), {
          params: {
            is_public: 'false',
            page: String(page),
            size: String(LOOKUP_PAGE_SIZE),
            sort: 'id',
            direction: 'asc',
          },
        }),
      );
      ids.push(...result.data.map((testCase) => testCase.id));
      if (!result.hasNext) {
        return ids;
      }
    }
  }

  removeTestCase(questionId: string, id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(testCaseUrl(questionId, id)));
  }
}

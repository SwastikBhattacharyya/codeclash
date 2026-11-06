import { DatePipe } from '@angular/common';
import { HttpErrorResponse, httpResource } from '@angular/common/http';
import { Component, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideTrash } from '@lucide/angular';
import { adminSection } from '../../../core/admin/admin-sections';
import type { Page } from '../../../core/api/page';
import { type PublicTestCase, testCaseUrl } from '../../../core/problems/problems-api';
import {
  failsOnTestCase,
  formatMemory,
  formatTime,
  LANGUAGE_LABELS,
  type Submission,
} from '../../../core/submissions/submissions';
import { Button } from '../../../ui/button';
import { CodeView } from '../../../ui/code-view';
import { CompileOutput } from '../../../ui/compile-output';
import { Dialog } from '../../../ui/dialog';
import { FieldIds } from '../../../ui/field-ids';
import { TestResultPanel } from '../../../ui/test-result-panel';
import { VerdictBadge } from '../../../ui/verdict-badge';
import type { TestCase } from '../questions/questions-api';
import { questionTestCaseIdsRequest } from './submissions-api';

const FORBIDDEN_STATUS = 403;
const NOT_FOUND_STATUS = 404;

@Component({
  selector: 'cc-submission-dialog',
  imports: [
    DatePipe,
    RouterLink,
    LucideTrash,
    Button,
    CodeView,
    CompileOutput,
    Dialog,
    TestResultPanel,
    VerdictBadge,
  ],
  templateUrl: './submission-dialog.html',
})
export class SubmissionDialog {
  readonly submission = input.required<Submission>();
  readonly questionTitle = input<string | null>(null);
  readonly canDelete = input(false);
  readonly closed = output<void>();
  readonly deleteRequested = output<Submission>();

  protected readonly summaryId = inject(FieldIds).create('submission-summary');
  protected readonly submissionsPath = adminSection('submissions').path;
  protected readonly questionsPath = adminSection('questions').path;
  protected readonly languageLabels = LANGUAGE_LABELS;
  protected readonly formatTime = formatTime;
  protected readonly formatMemory = formatMemory;

  protected readonly failed = computed(() => failsOnTestCase(this.submission().state));
  private readonly testCaseQuestionId = computed(() => {
    const submission = this.submission();
    return this.failed() && submission.failedTestCaseId !== null ? submission.questionId : null;
  });
  private readonly testCases = httpResource<Page<TestCase>>(() => {
    const questionId = this.testCaseQuestionId();
    return questionId ? questionTestCaseIdsRequest(questionId) : undefined;
  });
  protected readonly failedTestCase = httpResource<PublicTestCase>(() => {
    const submission = this.submission();
    return this.failed() && submission.failedTestCaseId !== null
      ? testCaseUrl(submission.questionId, submission.failedTestCaseId)
      : undefined;
  });
  protected readonly failedTestStatus = computed(() => {
    if (this.submission().failedTestCaseId === null) {
      return 'removed';
    }
    if (this.failedTestCase.hasValue()) {
      return 'loaded';
    }
    const error = this.failedTestCase.error();
    if (error instanceof HttpErrorResponse && error.status === FORBIDDEN_STATUS) {
      return 'hidden';
    }
    if (error instanceof HttpErrorResponse && error.status === NOT_FOUND_STATUS) {
      return 'removed';
    }
    return error ? 'unknown' : 'loading';
  });
  protected readonly failedTestNumber = computed(() => {
    const id = this.submission().failedTestCaseId;
    if (id === null || !this.testCases.hasValue()) {
      return null;
    }
    const index = this.testCases.value().data.findIndex((testCase) => testCase.id === id);
    return index === -1 ? null : index + 1;
  });
  protected readonly title = computed(() => `Submission by @${this.submission().username}`);
}

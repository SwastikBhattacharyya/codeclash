import { httpResource } from '@angular/common/http';
import { Component, computed, inject, linkedSignal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormField, FormRoot, form } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideCircleCheck } from '@lucide/angular';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import { AuthStore } from '../../../core/auth/auth-store';
import {
  CATEGORIES_URL,
  type Category,
  lookupRequest,
  PROBLEM_PATH,
  PROBLEMS_PATH,
  type Question,
  type QuestionQuery,
  questionsRequest,
  TAGS_URL,
  type Tag,
} from '../../../core/problems/problems-api';
import { type Submission, solvedRequest } from '../../../core/submissions/submissions';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { FIRST_PAGE, Pager } from '../../../ui/pager';
import { Spinner } from '../../../ui/spinner';
import { TextField } from '../../../ui/text-field';

type SearchModel = {
  title: string;
};

type SolvedSource = {
  userId: string | null;
  ids: string[] | null;
};

type SolvedSnapshot = {
  userId: string | null;
  ids: string[];
};

type Chip = {
  id: string;
  name: string;
};

@Component({
  selector: 'cc-problem-list',
  imports: [
    FormRoot,
    FormField,
    RouterLink,
    LucideCircleCheck,
    Alert,
    Button,
    Pager,
    Spinner,
    TextField,
  ],
  templateUrl: './problem-list.html',
})
export class ProblemList {
  private readonly router = inject(Router);
  private readonly auth = inject(AuthStore);
  private readonly userId = computed(() => this.auth.session()?.userId ?? null);
  private readonly params = toSignal(inject(ActivatedRoute).queryParamMap, {
    requireSync: true,
  });

  protected readonly problemPath = PROBLEM_PATH;
  protected readonly problemsPath = PROBLEMS_PATH;
  protected readonly query = computed<QuestionQuery>(() => {
    const params = this.params();
    const page = Number.parseInt(params.get('page') ?? '', 10);
    return {
      title: params.get('q')?.trim() ?? '',
      category: params.get('category'),
      tag: params.get('tag'),
      page: Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE,
    };
  });
  protected readonly hasFilters = computed(() => {
    const query = this.query();
    return query.title !== '' || query.category !== null || query.tag !== null;
  });

  protected readonly questions = httpResource<Page<Question>>(() => questionsRequest(this.query()));
  protected readonly categories = httpResource<Page<Category>>(() => lookupRequest(CATEGORIES_URL));
  protected readonly tags = httpResource<Page<Tag>>(() => lookupRequest(TAGS_URL));

  protected readonly categoryChips = computed<Chip[]>(() =>
    this.categories.hasValue() ? this.categories.value().data : [],
  );
  protected readonly tagChips = computed<Chip[]>(() =>
    this.tags.hasValue() ? this.tags.value().data : [],
  );
  protected readonly categoryNames = computed(
    () => new Map(this.categoryChips().map((chip) => [chip.id, chip.name])),
  );
  protected readonly tagNames = computed(
    () => new Map(this.tagChips().map((chip) => [chip.id, chip.name])),
  );

  protected readonly shownQuestions = lastValue(this.questions);
  protected readonly solved = httpResource<Page<Submission>>(() => {
    const userId = this.userId();
    const questionIds = this.shownQuestions()?.data.map((question) => question.id) ?? [];
    return userId === null || questionIds.length === 0
      ? undefined
      : solvedRequest(userId, questionIds);
  });
  private readonly shownSolved = linkedSignal<SolvedSource, SolvedSnapshot>({
    source: () => ({
      userId: this.userId(),
      ids: this.solved.hasValue()
        ? this.solved.value().data.map((submission) => submission.questionId)
        : null,
    }),
    computation: (source, previous) => {
      const kept =
        previous !== undefined && previous.value.userId === source.userId ? previous.value.ids : [];
      return { userId: source.userId, ids: source.ids ?? kept };
    },
  });
  private readonly solvedIds = computed(() => new Set(this.shownSolved().ids));
  private readonly titleParam = computed(() => this.params().get('q')?.trim() ?? '');
  private readonly searchModel = linkedSignal<SearchModel>(() => ({ title: this.titleParam() }));
  protected readonly searchForm = form(this.searchModel, {
    submission: {
      action: async (field) => {
        const title = field().value().title.trim();
        await this.router.navigate([PROBLEMS_PATH], {
          queryParams: { q: title === '' ? null : title, page: null },
          queryParamsHandling: 'merge',
        });
        return undefined;
      },
    },
  });

  protected chipParams(kind: 'category' | 'tag', id: string): Record<string, string | null> {
    const active = this.query()[kind] === id;
    return { [kind]: active ? null : id, page: null };
  }

  protected isSolved(question: Question): boolean {
    return this.solvedIds().has(question.id);
  }

  protected categoryLabels(question: Question): string[] {
    const names = this.categoryNames();
    return question.categories.flatMap((id) => names.get(id) ?? []);
  }

  protected tagLabels(question: Question): string[] {
    const names = this.tagNames();
    return question.tags.flatMap((id) => names.get(id) ?? []);
  }
}

create table contest_questions
(
  contest_id  uuid not null,
  question_id uuid not null,

  constraint pk_contest_questions primary key (contest_id, question_id),
  constraint fk_contest_questions_contest foreign key (contest_id)
    references contests (id) on delete cascade,
  constraint fk_contest_questions_question foreign key (question_id)
    references questions (id) on delete cascade
);

create index ix_contest_questions_question_id
  on contest_questions (question_id);

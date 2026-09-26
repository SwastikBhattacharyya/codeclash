create table submissions
(
  id          uuid        not null,
  question_id uuid        not null,
  user_id     uuid        not null,
  state       text        not null,
  created_at  timestamptz not null,
  updated_at  timestamptz not null,

  constraint pk_submissions primary key (id),
  constraint ck_submissions_id_version check (uuid_extract_version(id) = 7),
  constraint ck_submissions_state check (
    state in (
      'queued', 'compiled', 'compilation_error', 'correct', 'incorrect', 'runtime_error',
      'time_limit_exceeded', 'memory_limit_exceeded', 'runner_error'
    )
  )
);

create index ix_submissions_question_id_user_id
  on submissions (question_id, user_id);

create index ix_submissions_correct_question_id_user_id_created_at
  on submissions (question_id, user_id, created_at)
  where state = 'correct';

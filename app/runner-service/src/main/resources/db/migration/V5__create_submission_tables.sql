create table submissions
(
  id            uuid        not null,
  language      text        not null,
  code          text        not null,
  question_id   uuid        not null,
  status        text        not null,
  result        text,
  attempts      integer     not null default 0,
  claimed_at    timestamptz,
  trace_context jsonb       not null default '{}',
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),

  constraint pk_submissions primary key (id),
  constraint ck_submissions_id_version check (uuid_extract_version(id) = 7),
  constraint ck_submissions_language check (language in ('cpp', 'python', 'java')),
  constraint ck_submissions_status check (status in ('queued', 'running', 'done')),
  constraint ck_submissions_result check (
    result in (
      'queued', 'compiled', 'compilation_error', 'correct', 'incorrect', 'runtime_error',
      'time_limit_exceeded', 'memory_limit_exceeded', 'runner_error'
    )
  ),
  constraint ck_submissions_result_done check ((result is not null) = (status = 'done')),
  constraint ck_submissions_claimed_running check ((claimed_at is not null) = (status = 'running')),
  constraint ck_submissions_attempts_non_negative check (attempts >= 0),
  constraint ck_submissions_trace_context_object check (jsonb_typeof(trace_context) = 'object'),
  constraint fk_submissions_question
    foreign key (question_id) references questions (id) on delete cascade
);

create index ix_submissions_status_created_at
  on submissions (status, created_at);

create index ix_submissions_question_id
  on submissions (question_id);

create trigger trg_submissions_set_updated_at
  before update
  on submissions
  for each row
  when (old is distinct from new)
execute function set_updated_at();

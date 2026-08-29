create table submissions
(
  id                  uuid                 default uuidv7(),
  language            text        not null,
  code                text        not null,
  state               text        not null,
  failed_test_case_id uuid,
  time_taken_ms       bigint,
  memory_used_kb      bigint,
  compile_output      text,
  actual_output       text,
  question_id         uuid        not null,
  user_id             uuid        not null,
  created_at          timestamptz not null default now(),
  updated_at          timestamptz not null default now(),

  constraint pk_submissions primary key (id),
  constraint ck_submissions_id_version check (uuid_extract_version(id) = 7),
  constraint ck_submissions_language check (language in ('cpp', 'python', 'java')),
  constraint ck_submissions_code_not_blank check (code ~ '\S'),
  constraint ck_submissions_code_max_size check (char_length(code) <= 65536),
  constraint ck_submissions_state check (
    state in (
      'queued', 'compiled', 'compilation_error', 'correct', 'incorrect', 'runtime_error',
      'time_limit_exceeded', 'memory_limit_exceeded', 'runner_error'
    )
  ),
  constraint ck_submissions_failed_test_case_id_version check (
    uuid_extract_version(failed_test_case_id) = 7
  ),
  constraint ck_submissions_time_taken_ms_non_negative check (time_taken_ms >= 0),
  constraint ck_submissions_memory_used_kb_non_negative check (memory_used_kb >= 0),
  constraint ck_submissions_result_metrics check (
    ((time_taken_ms is not null and memory_used_kb is not null) = (state = 'correct'))
    and ((time_taken_ms is null) = (memory_used_kb is null))
  ),
  constraint ck_submissions_failed_test_case_state check (
    failed_test_case_id is null
    or state in ('incorrect', 'runtime_error', 'time_limit_exceeded', 'memory_limit_exceeded')
  ),
  constraint ck_submissions_compile_output_max_size check (char_length(compile_output) <= 65536),
  constraint ck_submissions_compile_output_state check (
    (compile_output is not null) = (state = 'compilation_error')
  ),
  constraint ck_submissions_actual_output_max_size check (char_length(actual_output) <= 65536),
  constraint ck_submissions_actual_output_state check (
    actual_output is null or state in ('incorrect', 'runtime_error')
  ),
  constraint fk_submissions_failed_test_case
    foreign key (failed_test_case_id) references test_cases (id) on delete set null,
  constraint fk_submissions_question
    foreign key (question_id) references questions (id) on delete cascade,
  constraint fk_submissions_user
    foreign key (user_id) references users (id) on delete cascade
);

create index ix_submissions_question_id
  on submissions (question_id);

create index ix_submissions_user_id
  on submissions (user_id);

create index ix_submissions_failed_test_case_id
  on submissions (failed_test_case_id);

create trigger trg_submissions_set_updated_at
  before update
  on submissions
  for each row
  when (old is distinct from new)
execute function set_updated_at();

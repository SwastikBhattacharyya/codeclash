create table questions
(
  id            uuid                 default uuidv7(),
  title         text        not null,
  summary       text        not null,
  description   text        not null,
  is_visible    bool        not null,
  max_run_time  integer     not null default 2000,
  max_ram_limit integer     not null default 256,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now(),

  constraint pk_questions primary key (id),
  constraint ck_questions_id_version check (uuid_extract_version(id) = 7),
  constraint ck_questions_title_min_size check (char_length(title) >= 8),
  constraint ck_questions_title_max_size check (char_length(title) <= 64),
  constraint ck_questions_title_single_line check (title !~ E'[\n\r\u0085\u2028\u2029]'),
  constraint ck_questions_title_trimmed check (is_stripped(title)),
  constraint ck_questions_summary_min_size check (char_length(summary) >= 16),
  constraint ck_questions_summary_max_size check (char_length(summary) <= 256),
  constraint ck_questions_summary_trimmed check (is_stripped(summary)),
  constraint ck_questions_description_min_size check (char_length(description) >= 32),
  constraint ck_questions_description_max_size check (char_length(description) <= 65535),
  constraint ck_questions_description_trimmed check (is_stripped(description)),
  constraint ck_questions_max_run_time_min_size check (max_run_time >= 100),
  constraint ck_questions_max_run_time_max_size check (max_run_time <= 10000),
  constraint ck_questions_max_ram_limit_min_size check (max_ram_limit >= 64),
  constraint ck_questions_max_ram_limit_max_size check (max_ram_limit <= 1024)
);

create trigger trg_questions_set_updated_at
  before update
  on questions
  for each row
  when (old is distinct from new)
execute function set_updated_at();

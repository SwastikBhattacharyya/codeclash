create table questions
(
  id            uuid        not null,
  max_run_time  integer     not null,
  max_ram_limit integer     not null,
  updated_at    timestamptz not null,

  constraint pk_questions primary key (id),
  constraint ck_questions_id_version check (uuid_extract_version(id) = 7),
  constraint ck_questions_max_run_time_min_size check (max_run_time >= 100),
  constraint ck_questions_max_run_time_max_size check (max_run_time <= 10000),
  constraint ck_questions_max_ram_limit_min_size check (max_ram_limit >= 64),
  constraint ck_questions_max_ram_limit_max_size check (max_ram_limit <= 1024)
);

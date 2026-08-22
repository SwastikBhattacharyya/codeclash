create table test_cases
(
  id          uuid                 default uuidv7(),
  input       text        not null,
  output      text        not null,
  is_public   bool        not null,
  question_id uuid        not null,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),

  constraint pk_test_cases primary key (id),
  constraint ck_test_cases_id_version check (uuid_extract_version(id) = 7),
  constraint ck_test_cases_input_max_size check (char_length(input) <= 1048576),
  constraint ck_test_cases_output_max_size check (char_length(output) <= 1048576),
  constraint fk_test_cases_question
    foreign key (question_id) references questions (id) on delete cascade
);

create index ix_test_cases_question_id
  on test_cases (question_id);

create trigger trg_test_cases_set_updated_at
  before update
  on test_cases
  for each row
  when (old is distinct from new)
execute function set_updated_at();

create table test_cases
(
  id          uuid        not null,
  input       text        not null,
  output      text        not null,
  is_public   bool        not null,
  question_id uuid        not null,
  created_at  timestamptz not null,
  updated_at  timestamptz not null,

  constraint pk_test_cases primary key (id),
  constraint ck_test_cases_id_version check (uuid_extract_version(id) = 7),
  constraint ck_test_cases_question_id_version check (uuid_extract_version(question_id) = 7),
  constraint fk_test_cases_question
    foreign key (question_id) references questions (id) on delete cascade
);

create index ix_test_cases_question_id
  on test_cases (question_id);

create table questions
(
  id         uuid        not null,
  updated_at timestamptz not null,

  constraint pk_questions primary key (id),
  constraint ck_questions_id_version check (uuid_extract_version(id) = 7)
);

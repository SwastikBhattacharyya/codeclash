create table question_tags
(
  question_id uuid not null,
  tag_id      uuid not null,

  constraint pk_question_tags primary key (question_id, tag_id),
  constraint fk_question_tags_question
    foreign key (question_id) references questions (id) on delete cascade,
  constraint fk_question_tags_tag
    foreign key (tag_id) references tags (id) on delete cascade
);

create index ix_question_tags_tag_id
  on question_tags (tag_id);

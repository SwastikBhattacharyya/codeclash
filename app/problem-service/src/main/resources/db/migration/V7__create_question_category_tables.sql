create table question_categories
(
  question_id uuid not null,
  category_id uuid not null,

  constraint pk_question_categories primary key (question_id, category_id),
  constraint fk_question_categories_question
    foreign key (question_id) references questions (id) on delete cascade,
  constraint fk_question_categories_category
    foreign key (category_id) references categories (id) on delete cascade
);

create index ix_question_categories_category_id
  on question_categories (category_id);

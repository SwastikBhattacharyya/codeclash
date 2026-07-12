create table role_permissions
(
  role_id       uuid not null,
  permission_id uuid not null,

  constraint pk_role_permissions primary key (role_id, permission_id),
  constraint fk_role_permissions_role
    foreign key (role_id) references roles (id) on delete cascade,
  constraint fk_role_permissions_permission
    foreign key (permission_id) references permissions (id) on delete cascade
);

create index ix_role_permissions_permission_id
  on role_permissions (permission_id);

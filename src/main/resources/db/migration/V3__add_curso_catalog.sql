create table if not exists curso (
  id bigserial primary key,
  consultora_id bigint not null,
  empresa varchar(180) not null,
  grupo varchar(180),
  activa boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint fk_curso_consultora
    foreign key (consultora_id) references consultora(id)
);

alter table clase
  add column if not exists curso_id bigint;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conname = 'fk_clase_curso'
  ) then
    alter table clase
      add constraint fk_clase_curso
        foreign key (curso_id) references curso(id);
  end if;
end $$;

create index if not exists idx_curso_consultora on curso (consultora_id);
create index if not exists idx_curso_empresa_grupo on curso (empresa, grupo);
create index if not exists idx_clase_curso on clase (curso_id);

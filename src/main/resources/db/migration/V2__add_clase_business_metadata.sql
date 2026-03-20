alter table clase
  add column if not exists empresa varchar(180);

alter table clase
  add column if not exists grupo varchar(180);

alter table clase
  add column if not exists facturable boolean not null default true;

alter table clase
  add column if not exists clasificacion_confirmada boolean not null default false;

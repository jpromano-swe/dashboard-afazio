create table if not exists teacher_note_document (
  id bigserial primary key,
  curso_id bigint not null references curso(id),
  periodo varchar(7) not null,
  drive_file_id varchar(255) not null,
  drive_url varchar(1000) not null,
  document_title varchar(255) not null,
  folder_path varchar(500) not null,
  source_file_count integer not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint uk_teacher_note_document_curso_periodo unique (curso_id, periodo)
);

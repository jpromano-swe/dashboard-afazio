create table consultora(
  id bigserial primary key,
  nombre varchar(20) not null,
  descripcion varchar(255),
  activa boolean not null default true,
  google_calendar_id varchar(255),
  require_reporte_excel boolean not null default false,
  created_at timestamptz not null default now(),
  update_at timestamptz not null default now(),
  constraint uq_consultora_nombre unique(nombre)
);

create table tarifa_consultora (
                                 id bigserial primary key,
                                 consultora_id bigint not null,
                                 monto_por_hora numeric(12,2) not null,
                                 moneda char(3) not null default 'ARS',
                                 vigente_desde date not null,
                                 vigente_hasta date,
                                 fecha_ultimo_aumento date,
                                 observaciones varchar(255),
                                 created_at timestamptz not null default now(),
                                 updated_at timestamptz not null default now(),
                                 constraint fk_tarifa_consultora_consultora
                                   foreign key (consultora_id) references consultora(id),
                                 constraint ck_tarifa_consultora_monto_positivo
                                   check (monto_por_hora > 0),
                                 constraint ck_tarifa_consultora_rango_fechas
                                   check (vigente_hasta is null or vigente_hasta >= vigente_desde)
);

create table clase (
                     id bigserial primary key,
                     consultora_id bigint not null,
                     titulo varchar(180) not null,
                     descripcion varchar(1000),
                     fecha_inicio timestamptz not null,
                     fecha_fin timestamptz not null,
                     duracion_minutos integer not null,
                     google_event_id varchar(255),
                     estado varchar(30) not null default 'PROGRAMADA',
                     sincronizada_en timestamptz,
                     created_at timestamptz not null default now(),
                     updated_at timestamptz not null default now(),
                     constraint fk_clase_consultora
                       foreign key (consultora_id) references consultora(id),
                     constraint uq_clase_google_event_id unique (google_event_id),
                     constraint ck_clase_fechas_validas
                       check (fecha_fin > fecha_inicio),
                     constraint ck_clase_duracion_positiva
                       check (duracion_minutos > 0),
                     constraint ck_clase_estado
                       check (estado in ('PROGRAMADA', 'DICTADA', 'CANCELADA', 'REPROGRAMADA'))
);

create table asistencia (
                          id bigserial primary key,
                          clase_id bigint not null,
                          estado varchar(30) not null,
                          marcada_en timestamptz not null default now(),
                          observacion varchar(500),
                          created_at timestamptz not null default now(),
                          updated_at timestamptz not null default now(),
                          constraint fk_asistencia_clase
                            foreign key (clase_id) references clase(id) on delete cascade,
                          constraint uq_asistencia_clase unique (clase_id),
                          constraint ck_asistencia_estado
                            check (estado in ('PENDIENTE', 'ASISTIO', 'AUSENTE', 'REPROGRAMADA'))
);

create index idx_tarifa_consultora_consultora on tarifa_consultora (consultora_id);
create index idx_tarifa_consultora_vigencia on tarifa_consultora (vigente_desde, vigente_hasta);
create index idx_clase_consultora on clase (consultora_id);
create index idx_clase_fecha_inicio on clase (fecha_inicio);
create index idx_asistencia_estado on asistencia (estado);

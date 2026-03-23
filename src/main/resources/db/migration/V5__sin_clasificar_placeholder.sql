insert into consultora (nombre, descripcion, activa, requiere_reporte_excel, google_calendar_id)
select 'Sin clasificar', 'Placeholder para clases importadas sin clasificar', false, false, null
where not exists (
  select 1
  from consultora
  where nombre = 'Sin clasificar'
);

update clase
set consultora_id = (
  select id
  from consultora
  where nombre = 'Sin clasificar'
)
where clasificacion_confirmada = false;

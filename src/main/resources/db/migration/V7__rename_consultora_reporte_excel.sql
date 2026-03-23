do $$
begin
  if exists (
    select 1
    from information_schema.columns
    where table_name = 'consultora'
      and column_name = 'require_reporte_excel'
  ) and not exists (
    select 1
    from information_schema.columns
    where table_name = 'consultora'
      and column_name = 'requiere_reporte_excel'
  ) then
    alter table consultora
      rename column require_reporte_excel to requiere_reporte_excel;
  end if;

  if exists (
    select 1
    from information_schema.columns
    where table_name = 'consultora'
      and column_name = 'update_at'
  ) and not exists (
    select 1
    from information_schema.columns
    where table_name = 'consultora'
      and column_name = 'updated_at'
  ) then
    alter table consultora
      rename column update_at to updated_at;
  end if;
end $$;

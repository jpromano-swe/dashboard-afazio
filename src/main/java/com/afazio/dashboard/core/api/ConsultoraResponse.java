package com.afazio.dashboard.core.api;

public record ConsultoraResponse(
  Long id,
  String nombre,
  String descripcion,
  boolean activa,
  boolean requiereReporteExcel,
  String googleCalendarId
) {
}

package com.afazio.dashboard.core.application;

public record CrearConsultoraCommand(
  String nombre,
  String descripcion,
  boolean requiereReporteExcel,
  String googleCalendarId
) {
}

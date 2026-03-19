package com.afazio.dashboard.reporting.application;

import java.math.BigDecimal;

public record ReporteHorasExcelRow(
  String empresa,
  String grupo,
  String horario,
  int clases,
  BigDecimal duracionClaseHoras,
  BigDecimal cantidadTotalHoras,
  BigDecimal honorarios,
  BigDecimal subtotal
) {
}

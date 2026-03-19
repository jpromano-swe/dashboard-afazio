package com.afazio.dashboard.reporting.application;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record ReporteHorasExcelData(
  String docenteNombre,
  String docenteCuit,
  String consultoraNombre,
  YearMonth periodo,
  List<ReporteHorasExcelRow> rows,
  BigDecimal total
) {
}

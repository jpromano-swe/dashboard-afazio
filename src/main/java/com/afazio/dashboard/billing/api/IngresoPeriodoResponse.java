package com.afazio.dashboard.billing.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record IngresoPeriodoResponse(
  LocalDate from,
  LocalDate to,
  int cantidadClases,
  BigDecimal total,
  List<IngresoDetalleResponse> detalle
) {
}

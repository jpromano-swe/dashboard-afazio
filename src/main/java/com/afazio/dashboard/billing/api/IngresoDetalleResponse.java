package com.afazio.dashboard.billing.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoDetalleResponse(
  Long claseId,
  String consultoraNombre,
  String tituloClase,
  LocalDate fechaClase,
  Integer duracionMinutos,
  BigDecimal montoPorHora,
  String moneda,
  BigDecimal importeCalculado
) {
}

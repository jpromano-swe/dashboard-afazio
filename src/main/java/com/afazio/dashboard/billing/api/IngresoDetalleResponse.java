package com.afazio.dashboard.billing.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoDetalleResponse(
  Long claseId,
  Long cursoId,
  String empresa,
  String grupo,
  String consultoraNombre,
  String tituloClase,
  LocalDate fechaClase,
  Integer duracionMinutos,
  BigDecimal montoPorHora,
  String moneda,
  boolean facturable,
  boolean sinClasificar,
  BigDecimal importeCalculado
) {
}

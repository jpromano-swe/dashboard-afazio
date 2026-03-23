package com.afazio.dashboard.billing.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IngresoPorClaseResponse(
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
  BigDecimal importeCalculado
) {
}

package com.afazio.dashboard.billing.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaConsultoraResponse(
  Long id,
  Long consultoraId,
  String consultoraNombre,
  BigDecimal montoPorHora,
  String moneda,
  LocalDate vigenteDesde,
  LocalDate vigenteHasta,
  LocalDate fechaUltimoAumento,
  String observaciones
) {
}

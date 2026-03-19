package com.afazio.dashboard.billing.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearTarifaConsultoraCommand(
  Long consultoraId,
  BigDecimal montoPorHora,
  String moneda,
  LocalDate vigenteDesde,
  LocalDate vigenteHasta,
  LocalDate fechaUltimoAumento,
  String observaciones
) {
}

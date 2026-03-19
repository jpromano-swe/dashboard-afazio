package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.domain.AsistenciaEstado;

import java.time.OffsetDateTime;

public record AsistenciaResponse(
  Long id,
  Long claseId,
  AsistenciaEstado estado,
  String observacion,
  OffsetDateTime marcadaEn
) {
}

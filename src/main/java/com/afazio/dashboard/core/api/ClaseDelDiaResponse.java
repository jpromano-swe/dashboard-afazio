package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.domain.AsistenciaEstado;
import com.afazio.dashboard.core.domain.ClaseEstado;

import java.time.OffsetDateTime;

public record ClaseDelDiaResponse(
  Long id,
  String consultoraNombre,
  String titulo,
  String descripcion,
  OffsetDateTime fechaInicio,
  OffsetDateTime fechaFin,
  Integer duracionMinutos,
  ClaseEstado estado,
  String googleEventId,
  AsistenciaEstado asistenciaEstado,
  String asistenciaObservacion,
  OffsetDateTime asistenciaMarcadaEn
) {
}

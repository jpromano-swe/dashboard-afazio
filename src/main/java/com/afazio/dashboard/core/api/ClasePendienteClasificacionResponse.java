package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.domain.ClaseEstado;

import java.time.OffsetDateTime;

public record ClasePendienteClasificacionResponse(
  Long id,
  Long cursoId,
  String titulo,
  String descripcion,
  String meetingUrl,
  String empresa,
  String grupo,
  boolean facturable,
  boolean clasificacionConfirmada,
  String consultoraNombre,
  OffsetDateTime fechaInicio,
  OffsetDateTime fechaFin,
  Integer duracionMinutos,
  ClaseEstado estado,
  String googleEventId
) {
}

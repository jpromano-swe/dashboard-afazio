package com.afazio.dashboard.core.api;

public record ActualizacionClasificacionMasivaResponse(
  String titulo,
  int processed,
  Long cursoId,
  String consultoraNombre,
  String empresa,
  String grupo,
  boolean facturable,
  boolean clasificacionConfirmada
) {
}

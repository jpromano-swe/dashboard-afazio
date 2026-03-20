package com.afazio.dashboard.core.api;

public record CursoResponse(
  Long id,
  Long consultoraId,
  String consultoraNombre,
  String empresa,
  String grupo,
  boolean activa
) {
}

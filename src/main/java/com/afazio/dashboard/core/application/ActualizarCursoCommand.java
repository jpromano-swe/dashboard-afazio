package com.afazio.dashboard.core.application;

public record ActualizarCursoCommand(
  Long consultoraId,
  String empresa,
  String grupo,
  Boolean activa
) {
}

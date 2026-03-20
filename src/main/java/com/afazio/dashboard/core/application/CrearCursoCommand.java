package com.afazio.dashboard.core.application;

public record CrearCursoCommand(
  Long consultoraId,
  String empresa,
  String grupo,
  Boolean activa
) {
}

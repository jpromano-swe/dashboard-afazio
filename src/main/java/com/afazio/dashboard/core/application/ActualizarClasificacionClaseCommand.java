package com.afazio.dashboard.core.application;

public record ActualizarClasificacionClaseCommand(
  Long cursoId,
  Long consultoraId,
  String empresa,
  String grupo,
  boolean facturable,
  boolean clasificacionConfirmada
) {
}

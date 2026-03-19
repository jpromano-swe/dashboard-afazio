package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.domain.AsistenciaEstado;

public record MarcarAsistenciaCommand(
  Long claseId,
  AsistenciaEstado estado,
  String observacion
) {
}

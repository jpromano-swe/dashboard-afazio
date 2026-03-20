package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.domain.ClaseEstado;

public record ActualizarEstadoClaseCommand(
  ClaseEstado estado
) {
}

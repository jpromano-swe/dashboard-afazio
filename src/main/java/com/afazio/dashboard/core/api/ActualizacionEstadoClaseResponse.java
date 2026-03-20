package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.domain.ClaseEstado;

public record ActualizacionEstadoClaseResponse(
  Long id,
  String titulo,
  ClaseEstado estadoAnterior,
  ClaseEstado estadoActual
) {
}

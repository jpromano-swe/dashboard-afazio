package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.domain.Clase;

public final class ClaseDisplayNames {

  public static final String PLACEHOLDER_CONSULTORA_NOMBRE = "Sin clasificar";

  private ClaseDisplayNames() {
  }

  public static String consultoraNombreVisible(Clase clase) {
    if (clase == null) {
      return null;
    }

    if (!clase.isClasificacionConfirmada()) {
      return PLACEHOLDER_CONSULTORA_NOMBRE;
    }

    return clase.getConsultora() != null ? clase.getConsultora().getNombre() : PLACEHOLDER_CONSULTORA_NOMBRE;
  }

  public static boolean esSinClasificar(Clase clase) {
    if (clase == null) {
      return true;
    }

    if (!clase.isClasificacionConfirmada()) {
      return true;
    }

    return clase.getConsultora() == null
      || PLACEHOLDER_CONSULTORA_NOMBRE.equalsIgnoreCase(clase.getConsultora().getNombre());
  }
}

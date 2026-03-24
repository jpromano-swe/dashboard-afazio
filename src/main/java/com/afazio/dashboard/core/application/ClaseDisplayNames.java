package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.domain.Clase;

import java.util.Locale;

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
    return esNombreSinClasificar(consultoraNombreVisible(clase));
  }

  public static boolean esNombreSinClasificar(String nombre) {
    if (nombre == null) {
      return true;
    }

    return normalizarNombre(nombre).equals(normalizarNombre(PLACEHOLDER_CONSULTORA_NOMBRE));
  }

  private static String normalizarNombre(String nombre) {
    return nombre
      .trim()
      .replaceAll("\\s+", " ")
      .toLowerCase(Locale.ROOT);
  }
}

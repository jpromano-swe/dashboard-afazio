package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.Consultora;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaseDisplayNamesTest {

  @Test
  void detectaSinClasificarCuandoLaClaseNoEstaConfirmada() {
    Clase clase = new Clase();
    clase.setClasificacionConfirmada(false);

    assertTrue(ClaseDisplayNames.esSinClasificar(clase));
  }

  @Test
  void detectaSinClasificarCuandoElNombreVisibleTieneEspaciosOMayusculas() {
    assertTrue(ClaseDisplayNames.esNombreSinClasificar("  SIN   CLASIFICAR  "));
  }

  @Test
  void noMarcaComoSinClasificarUnaConsultoraReal() {
    Consultora consultora = new Consultora();
    consultora.setNombre("Globant");

    Clase clase = new Clase();
    clase.setClasificacionConfirmada(true);
    clase.setConsultora(consultora);

    assertFalse(ClaseDisplayNames.esSinClasificar(clase));
  }
}

package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import com.afazio.dashboard.core.infrastructure.TarifaConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ObtenerTarifaVigenteService {

  private final TarifaConsultoraRepository tarifaConsultoraRepository;

  public ObtenerTarifaVigenteService(TarifaConsultoraRepository tarifaConsultoraRepository) {
    this.tarifaConsultoraRepository = tarifaConsultoraRepository;
  }

  @Transactional(readOnly = true)
  public TarifaConsultora ejecutar(Consultora consultora, LocalDate fecha) {
    List<TarifaConsultora> tarifas = tarifaConsultoraRepository.findVigentesByConsultoraAndFecha(consultora, fecha);

    if (tarifas.isEmpty()) {
      throw new IllegalArgumentException(
        "No existe una tarifa vigente para la consultora " + consultora.getNombre() + " en la fecha " + fecha
      );
    }

    return tarifas.getFirst();
  }
}

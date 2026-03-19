package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.TarifaConsultoraResponse;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ObtenerTarifaVigentePorConsultoraService {

  private final ConsultoraRepository consultoraRepository;
  private final ObtenerTarifaVigenteService obtenerTarifaVigenteService;

  public ObtenerTarifaVigentePorConsultoraService(
    ConsultoraRepository consultoraRepository,
    ObtenerTarifaVigenteService obtenerTarifaVigenteService
  ) {
    this.consultoraRepository = consultoraRepository;
    this.obtenerTarifaVigenteService = obtenerTarifaVigenteService;
  }

  @Transactional(readOnly = true)
  public TarifaConsultoraResponse ejecutar(Long consultoraId, LocalDate fecha) {
    Consultora consultora = consultoraRepository.findById(consultoraId)
      .orElseThrow(() -> new IllegalArgumentException(
        "No existe la consultora con el id " + consultoraId
      ));

    TarifaConsultora tarifa = obtenerTarifaVigenteService.ejecutar(consultora, fecha);

    return new TarifaConsultoraResponse(
      tarifa.getId(),
      tarifa.getConsultora().getId(),
      tarifa.getConsultora().getNombre(),
      tarifa.getMontoPorHora(),
      tarifa.getMoneda(),
      tarifa.getVigenteDesde(),
      tarifa.getVigenteHasta(),
      tarifa.getFechaUltimoAumento(),
      tarifa.getObservaciones()
    );
  }
}

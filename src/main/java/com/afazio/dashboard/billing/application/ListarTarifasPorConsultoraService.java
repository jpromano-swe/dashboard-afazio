package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.TarifaConsultoraResponse;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import com.afazio.dashboard.core.infrastructure.TarifaConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarTarifasPorConsultoraService {

  private final ConsultoraRepository consultoraRepository;
  private final TarifaConsultoraRepository tarifaConsultoraRepository;

  public ListarTarifasPorConsultoraService(
    ConsultoraRepository consultoraRepository,
    TarifaConsultoraRepository tarifaConsultoraRepository
  ) {
    this.consultoraRepository = consultoraRepository;
    this.tarifaConsultoraRepository = tarifaConsultoraRepository;
  }

  @Transactional(readOnly = true)
  public List<TarifaConsultoraResponse> ejecutar(Long consultoraId) {
    Consultora consultora = consultoraRepository.findById(consultoraId)
      .orElseThrow(() -> new IllegalArgumentException(
        "No existe la consultora con el id " + consultoraId
      ));

    List<TarifaConsultora> tarifas = tarifaConsultoraRepository.findByConsultoraOrderByVigenteDesdeDesc(consultora);

    return tarifas.stream()
      .map(tarifa -> new TarifaConsultoraResponse(
        tarifa.getId(),
        tarifa.getConsultora().getId(),
        tarifa.getConsultora().getNombre(),
        tarifa.getMontoPorHora(),
        tarifa.getMoneda(),
        tarifa.getVigenteDesde(),
        tarifa.getVigenteHasta(),
        tarifa.getFechaUltimoAumento(),
        tarifa.getObservaciones()
      ))
      .toList();
  }
}

package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.ConsultoraResponse;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarConsultorasService {

  private final ConsultoraRepository consultoraRepository;

  public ListarConsultorasService(ConsultoraRepository consultoraRepository) {
    this.consultoraRepository = consultoraRepository;
  }

  @Transactional(readOnly = true)
  public List<ConsultoraResponse> ejecutar() {
    return consultoraRepository.findAllByOrderByNombreAsc().stream()
      .map(consultora -> new ConsultoraResponse(
        consultora.getId(),
        consultora.getNombre(),
        consultora.getDescripcion(),
        consultora.isActiva(),
        consultora.isRequiereReporteExcel(),
        consultora.getGoogleCalendarId()
      ))
      .toList();
  }
}

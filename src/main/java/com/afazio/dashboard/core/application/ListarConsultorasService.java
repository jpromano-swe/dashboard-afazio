package com.afazio.dashboard.core.application;

import com.afazio.dashboard.calendar.application.GoogleCalendarProperties;
import com.afazio.dashboard.core.api.ConsultoraResponse;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarConsultorasService {

  private final ConsultoraRepository consultoraRepository;
  private final GoogleCalendarProperties googleCalendarProperties;

  public ListarConsultorasService(
    ConsultoraRepository consultoraRepository,
    GoogleCalendarProperties googleCalendarProperties
  ) {
    this.consultoraRepository = consultoraRepository;
    this.googleCalendarProperties = googleCalendarProperties;
  }

  @Transactional(readOnly = true)
  public List<ConsultoraResponse> ejecutar() {
    return consultoraRepository.findAllByOrderByNombreAsc().stream()
      .filter(consultora -> consultora.isActiva())
      .map(consultora -> new ConsultoraResponse(
        consultora.getId(),
        consultora.getNombre(),
        consultora.getDescripcion(),
        consultora.isActiva(),
        consultora.isRequiereReporteExcel(),
        googleCalendarProperties.calendarId()
      ))
      .toList();
  }
}

package com.afazio.dashboard.core.application;

import com.afazio.dashboard.calendar.application.GoogleCalendarProperties;
import com.afazio.dashboard.core.api.ConsultoraResponse;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CrearConsultoraService {

  private final ConsultoraRepository consultoraRepository;
  private final GoogleCalendarProperties googleCalendarProperties;

  public CrearConsultoraService(
    ConsultoraRepository consultoraRepository,
    GoogleCalendarProperties googleCalendarProperties
  ) {
    this.consultoraRepository = consultoraRepository;
    this.googleCalendarProperties = googleCalendarProperties;
  }

  @Transactional
  public ConsultoraResponse ejecutar(CrearConsultoraCommand command){
    consultoraRepository.findByNombre(command.nombre())
      .ifPresent(existing -> {
        throw new IllegalArgumentException("Ya existe una consultora con ese nombre");
      });

    Consultora consultora = new Consultora();
    consultora.setNombre(command.nombre());
    consultora.setDescripcion(command.descripcion());
    consultora.setActiva(true);
    consultora.setRequiereReporteExcel(command.requiereReporteExcel());
    consultora.setGoogleCalendarId(googleCalendarProperties.calendarId());

    Consultora saved = consultoraRepository.save(consultora);

    return new ConsultoraResponse(
        saved.getId(),
        saved.getNombre(),
        saved.getDescripcion(),
        saved.isActiva(),
        saved.isRequiereReporteExcel(),
        googleCalendarProperties.calendarId()
    );
  }
}

package com.afazio.dashboard.calendar.application;

import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.ClaseEstado;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SincronizarClasesService {

  private final CalendarProvider calendarProvider;
  private final ConsultoraRepository consultoraRepository;
  private final ClaseRepository claseRepository;


  public SincronizarClasesService(
    CalendarProvider calendarProvider,
    ConsultoraRepository consultoraRepository,
    ClaseRepository claseRepository
  ) {

    this.calendarProvider = calendarProvider;
    this.consultoraRepository = consultoraRepository;
    this.claseRepository = claseRepository;
  }

  public int ejecutar(OffsetDateTime from, OffsetDateTime to) {
    List<CalendarClassEvent> events = calendarProvider.getClassesBetween(from, to);
    Set<String> idsPresentes = construirIdsPresentes(events);

    int processed = 0;

    for (CalendarClassEvent event : events) {
      Consultora consultora = consultoraRepository.findByNombre(event.consultoraNombre())
        .orElseThrow(() -> new IllegalArgumentException(
          "No existe una consultora para el evento: " + event.consultoraNombre()
        ));

      Clase clase = claseRepository.findByGoogleEventId(event.externalEventId())
        .orElseGet(Clase::new);

      clase.setConsultora(consultora);
      clase.setTitulo(event.title());
      clase.setDescripcion(event.description());
      clase.setMeetingUrl(event.meetingUrl());
      clase.setFechaInicio(event.startAt());
      clase.setFechaFin(event.endAt());
      clase.setDuracionMinutos((int) Duration.between(event.startAt(), event.endAt()).toMinutes());
      clase.setGoogleEventId(event.externalEventId());
      clase.setEstado(ClaseEstado.PROGRAMADA);
      clase.setSincronizadaEn(OffsetDateTime.now());

      claseRepository.save(clase);
      processed++;
    }
    limpiarClasesFueraDelFeed(from, to, idsPresentes);
    return processed;
  }

  private void limpiarClasesFueraDelFeed(OffsetDateTime from, OffsetDateTime to, Set<String> idsPresentes) {
    List<Clase> clasesFueraDelFeed = claseRepository.findByFechaInicioBetweenOrderByFechaInicioAsc(from, to).stream()
      .filter(clase -> clase.getGoogleEventId() != null && !clase.getGoogleEventId().isBlank())
      .filter(clase -> !idsPresentes.contains(clase.getGoogleEventId()))
      .toList();

    if (!clasesFueraDelFeed.isEmpty()) {
      claseRepository.deleteAll(clasesFueraDelFeed);
    }
  }

  private Set<String> construirIdsPresentes(List<CalendarClassEvent> events) {
    Set<String> ids = new LinkedHashSet<>();
    for (CalendarClassEvent event : events) {
      ids.add(event.externalEventId());
    }
    return ids;
  }
}

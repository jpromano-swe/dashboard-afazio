package com.afazio.dashboard.calendar.application;

import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.ClaseEstado;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.truncate;

@Service
public class SincronizarClasesDesdeGoogleService {

  private final GoogleCalendarEventFetcher googleCalendarEventFetcher;
  private final ConsultoraRepository consultoraRepository;
  private final ClaseRepository claseRepository;

  public SincronizarClasesDesdeGoogleService(
    GoogleCalendarEventFetcher googleCalendarEventFetcher,
    ConsultoraRepository consultoraRepository,
    ClaseRepository claseRepository
  ) {
    this.googleCalendarEventFetcher = googleCalendarEventFetcher;
    this.consultoraRepository = consultoraRepository;
    this.claseRepository = claseRepository;
  }

  @Transactional
  public int ejecutar(
    OAuth2AuthorizedClient authorizedClient,
    OffsetDateTime from,
    OffsetDateTime to
  ) {
    List<CalendarClassEvent> events = googleCalendarEventFetcher.fetch(authorizedClient, from, to);

    int processed = 0;

    for (CalendarClassEvent event : events) {
      Consultora consultora = consultoraRepository.findByNombre(event.consultoraNombre())
        .orElseThrow(() -> new IllegalArgumentException(
          "No existe una consultora para el evento: " + event.consultoraNombre()
        ));

      boolean isNewClase = false;
      Clase clase = claseRepository.findByGoogleEventId(event.externalEventId())
        .orElseGet(() -> {
          Clase nuevaClase = new Clase();
          nuevaClase.setEmpresa(null);
          nuevaClase.setGrupo(null);
          nuevaClase.setFacturable(true);
          nuevaClase.setClasificacionConfirmada(false);
          return nuevaClase;
        });

      if (clase.getId() == null) {
        isNewClase = true;
      }


      clase.setConsultora(consultora);
      clase.setTitulo(event.title());
      clase.setDescripcion(truncate(event.description()));
      clase.setMeetingUrl(truncate(event.meetingUrl()));
      clase.setFechaInicio(event.startAt());
      clase.setFechaFin(event.endAt());
      clase.setDuracionMinutos((int) Duration.between(event.startAt(), event.endAt()).toMinutes());
      clase.setGoogleEventId(event.externalEventId());
      clase.setEstado(ClaseEstado.PROGRAMADA);
      clase.setSincronizadaEn(OffsetDateTime.now());

      if (isNewClase) {
        aplicarClasificacionExistenteOValoresPorDefecto(clase, consultora);
      }

      claseRepository.save(clase);
      processed++;
    }
    return processed;
  }

  private void aplicarClasificacionExistenteOValoresPorDefecto(Clase clase, Consultora consultora) {
    buscarClaseClasificadaRelacionada(clase, consultora)
      .ifPresentOrElse(
        claseRelacionada -> {
          clase.setCurso(claseRelacionada.getCurso());
          clase.setConsultora(claseRelacionada.getConsultora());
          clase.setEmpresa(claseRelacionada.getEmpresa());
          clase.setGrupo(claseRelacionada.getGrupo());
          clase.setFacturable(claseRelacionada.isFacturable());
          clase.setClasificacionConfirmada(claseRelacionada.isClasificacionConfirmada());
        },
        () -> {
          clase.setCurso(null);
          clase.setEmpresa(null);
          clase.setGrupo(null);
          clase.setFacturable(true);
          clase.setClasificacionConfirmada(false);
        }
      );
  }

  private java.util.Optional<Clase> buscarClaseClasificadaRelacionada(Clase clase, Consultora consultora) {
    String serieGoogle = extractSerieGoogle(clase.getGoogleEventId());
    if (serieGoogle != null) {
      var porSerie = claseRepository
        .findFirstByConsultoraAndGoogleEventIdStartingWithAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(
          consultora,
          serieGoogle + "_"
        );
      if (porSerie.isPresent()) {
        return porSerie;
      }
    }

    return claseRepository.findFirstByConsultoraAndTituloAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(
      consultora,
      clase.getTitulo()
    );
  }

  private String extractSerieGoogle(String googleEventId) {
    if (googleEventId == null || googleEventId.isBlank()) {
      return null;
    }

    int separatorIndex = googleEventId.indexOf('_');
    if (separatorIndex <= 0) {
      return null;
    }

    return googleEventId.substring(0, separatorIndex);
  }

  private String truncate(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= 1000 ? value : value.substring(0, 1000);
  }

}

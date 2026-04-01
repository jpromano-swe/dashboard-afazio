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
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;

import static org.springframework.util.StringUtils.truncate;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class SincronizarClasesDesdeGoogleService {

  private static final String PLACEHOLDER_CONSULTORA_NOMBRE = "Sin clasificar";

  private final GoogleCalendarEventFetcher googleCalendarEventFetcher;
  private final GoogleCalendarProperties googleCalendarProperties;
  private final ConsultoraRepository consultoraRepository;
  private final ClaseRepository claseRepository;

  public SincronizarClasesDesdeGoogleService(
    GoogleCalendarEventFetcher googleCalendarEventFetcher,
    GoogleCalendarProperties googleCalendarProperties,
    ConsultoraRepository consultoraRepository,
    ClaseRepository claseRepository
  ) {
    this.googleCalendarEventFetcher = googleCalendarEventFetcher;
    this.googleCalendarProperties = googleCalendarProperties;
    this.consultoraRepository = consultoraRepository;
    this.claseRepository = claseRepository;
  }

  @Transactional
  public int ejecutar(
    OAuth2AuthorizedClient authorizedClient,
    OffsetDateTime from,
    OffsetDateTime to
  ) {
    String sharedCalendarId = googleCalendarProperties.calendarId();
    if (!StringUtils.hasText(sharedCalendarId)) {
      throw new IllegalStateException("No está configurado app.google.calendar-id");
    }

    Consultora consultoraPorDefecto = consultoraRepository.findByNombre(PLACEHOLDER_CONSULTORA_NOMBRE)
      .orElseThrow(() -> new IllegalStateException(
        "No existe la consultora placeholder '" + PLACEHOLDER_CONSULTORA_NOMBRE + "'"
      ));

    List<CalendarClassEvent> events = googleCalendarEventFetcher.fetch(
      authorizedClient,
      sharedCalendarId,
      null,
      from,
      to
    );

    Set<String> eventIdsPresentes = construirIdsPresentes(sharedCalendarId, events);
    limpiarEventosFueraDelFeed(from, to, eventIdsPresentes);
    limpiarEventosIgnorados(from, to);

    int processed = 0;
    for (CalendarClassEvent event : events) {
      processed += sincronizarEvento(consultoraPorDefecto, event);
    }

    return processed;
  }

  private int sincronizarEvento(Consultora consultora, CalendarClassEvent event) {
    String normalizedEventId = normalizeGoogleEventId(event.sourceCalendarId(), event.externalEventId());

    Clase clase = claseRepository.findByGoogleEventId(normalizedEventId)
      .or(() -> claseRepository.findByGoogleEventId(event.externalEventId()))
      .orElseGet(() -> {
        Clase nuevaClase = new Clase();
        nuevaClase.setEmpresa(null);
        nuevaClase.setGrupo(null);
        nuevaClase.setFacturable(true);
        nuevaClase.setClasificacionConfirmada(false);
        return nuevaClase;
      });

    boolean isNewClase = clase.getId() == null;
    boolean debeIntentarHeredarClasificacion = isNewClase || !clase.isClasificacionConfirmada();

    // Populate the lookup keys first so recurring/title-based inheritance can work for new rows.
    clase.setTitulo(event.title());
    clase.setGoogleEventId(normalizedEventId);

    if (debeIntentarHeredarClasificacion) {
      aplicarClasificacionExistenteOValoresPorDefecto(clase, consultora, !isNewClase);
    }

    if (clase.getConsultora() == null) {
      clase.setConsultora(consultora);
    }

    clase.setDescripcion(truncate(event.description()));
    clase.setMeetingUrl(truncate(event.meetingUrl()));
    clase.setFechaInicio(event.startAt());
    clase.setFechaFin(event.endAt());
    clase.setDuracionMinutos((int) Duration.between(event.startAt(), event.endAt()).toMinutes());
    clase.setEstado(ClaseEstado.PROGRAMADA);
    clase.setSincronizadaEn(OffsetDateTime.now());

    claseRepository.save(clase);
    return 1;
  }

  private void aplicarClasificacionExistenteOValoresPorDefecto(
    Clase clase,
    Consultora consultora,
    boolean preservarValoresExistentesSiNoEncuentraRelacionada
  ) {
    buscarClaseClasificadaRelacionada(clase)
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
          if (preservarValoresExistentesSiNoEncuentraRelacionada) {
            return;
          }

          clase.setCurso(null);
          clase.setConsultora(consultora);
          clase.setEmpresa(null);
          clase.setGrupo(null);
          clase.setFacturable(true);
          clase.setClasificacionConfirmada(false);
        }
      );
  }

  private java.util.Optional<Clase> buscarClaseClasificadaRelacionada(Clase clase) {
    String serieGoogle = extractSerieGoogle(clase.getGoogleEventId());
    if (serieGoogle != null) {
      var porSerie = claseRepository
        .findFirstByGoogleEventIdStartingWithAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(
          serieGoogle + "_"
        );
      if (porSerie.isPresent()) {
        return porSerie;
      }
    }

    return claseRepository.findFirstByTituloAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(clase.getTitulo());
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

  private String normalizeGoogleEventId(String calendarId, String eventId) {
    return calendarId + "::" + eventId;
  }

  private String truncate(String value) {
    if (value == null) {
      return null;
    }
    return value.length() <= 1000 ? value : value.substring(0, 1000);
  }

  private void limpiarEventosIgnorados(OffsetDateTime from, OffsetDateTime to) {
    List<Clase> clasesIgnoradas = claseRepository.findByFechaInicioBetweenOrderByFechaInicioAsc(from, to).stream()
      .filter(clase -> shouldSkipEvent(clase.getTitulo()))
      .toList();

    if (!clasesIgnoradas.isEmpty()) {
      claseRepository.deleteAll(new ArrayList<>(clasesIgnoradas));
    }
  }

  private void limpiarEventosFueraDelFeed(OffsetDateTime from, OffsetDateTime to, Set<String> eventIdsPresentes) {
    List<Clase> clasesFueraDelFeed = claseRepository.findByFechaInicioBetweenOrderByFechaInicioAsc(from, to).stream()
      .filter(clase -> clase.getGoogleEventId() != null && !clase.getGoogleEventId().isBlank())
      .filter(clase -> !eventIdsPresentes.contains(clase.getGoogleEventId()))
      .toList();

    if (!clasesFueraDelFeed.isEmpty()) {
      claseRepository.deleteAll(new ArrayList<>(clasesFueraDelFeed));
    }
  }

  private Set<String> construirIdsPresentes(String sharedCalendarId, List<CalendarClassEvent> events) {
    Set<String> ids = new LinkedHashSet<>();
    for (CalendarClassEvent event : events) {
      ids.add(event.externalEventId());
      ids.add(normalizeGoogleEventId(sharedCalendarId, event.externalEventId()));
    }
    return ids;
  }

  private boolean shouldSkipEvent(String title) {
    if (!StringUtils.hasText(title)) {
      return false;
    }

    String normalized = title.toLowerCase().trim();
    return normalized.contains("cumple")
      || normalized.contains("contenido")
      || normalized.contains("lanzam")
      || normalized.contains("facturar")
      || normalized.contains("mamá")
      || normalized.equals("juanpi")
      || normalized.equals("blc");
  }

}

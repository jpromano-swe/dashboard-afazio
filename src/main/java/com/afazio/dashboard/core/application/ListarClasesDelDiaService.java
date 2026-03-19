package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.ClaseDelDiaResponse;
import com.afazio.dashboard.core.domain.Asistencia;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.infrastructure.AsistenciaRepository;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListarClasesDelDiaService {

  private final ClaseRepository claseRepository;
  private final AsistenciaRepository asistenciaRepository;

  public ListarClasesDelDiaService(
    ClaseRepository claseRepository,
    AsistenciaRepository asistenciaRepository
  ) {
    this.claseRepository = claseRepository;
    this.asistenciaRepository = asistenciaRepository;
  }

  @Transactional(readOnly = true)
  public List<ClaseDelDiaResponse> ejecutar(LocalDate fecha) {
    OffsetDateTime from = fecha.atStartOfDay().atOffset(ZoneOffset.ofHours(-3));
    OffsetDateTime to = fecha.plusDays(1).atStartOfDay().atOffset(ZoneOffset.ofHours(-3)).minusNanos(1);

    List<Clase> clases = claseRepository.findByFechaInicioBetweenOrderByFechaInicioAsc(from, to);

    Map<Long, Asistencia> asistenciasPorClaseId = asistenciaRepository.findByClaseIn(clases).stream()
      .collect(Collectors.toMap(asistencia -> asistencia.getClase().getId(), Function.identity()));

    return clases.stream()
      .map(clase -> {
        Asistencia asistencia = asistenciasPorClaseId.get(clase.getId());

        return new ClaseDelDiaResponse(
          clase.getId(),
          clase.getConsultora().getNombre(),
          clase.getTitulo(),
          clase.getDescripcion(),
          clase.getFechaInicio(),
          clase.getFechaFin(),
          clase.getDuracionMinutos(),
          clase.getEstado(),
          clase.getGoogleEventId(),
          asistencia != null ? asistencia.getEstado() : null,
          asistencia != null ? asistencia.getObservacion() : null,
          asistencia != null ? asistencia.getMarcadaEn() : null
        );
      })
      .toList();
  }
}

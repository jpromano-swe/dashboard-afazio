package com.afazio.dashboard.core.application;


import com.afazio.dashboard.core.api.AsistenciaResponse;
import com.afazio.dashboard.core.domain.Asistencia;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.infrastructure.AsistenciaRepository;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class MarcarAsistenciaService {

  private final AsistenciaRepository asistenciaRepository;
  private final ClaseRepository claseRepository;

  public MarcarAsistenciaService(
    AsistenciaRepository asistenciaRepository,
    ClaseRepository claseRepository
  ) {
    this.asistenciaRepository = asistenciaRepository;
    this.claseRepository = claseRepository;
  }

  public AsistenciaResponse ejecutar(MarcarAsistenciaCommand command) {
    Clase clase = claseRepository.findById(command.claseId())
      .orElseThrow(() -> new IllegalArgumentException("No existe la clase con el id " + command.claseId()));

    Asistencia asistencia = asistenciaRepository.findByClase(clase)
      .orElseGet(Asistencia::new);

    asistencia.setClase(clase);
    asistencia.setEstado(command.estado());
    asistencia.setObservacion(command.observacion());
    asistencia.setMarcadaEn(OffsetDateTime.now());

    Asistencia saved = asistenciaRepository.save(asistencia);

    return new AsistenciaResponse(
      saved.getId(),
      saved.getClase().getId(),
      saved.getEstado(),
      saved.getObservacion(),
      saved.getMarcadaEn()
    );
  }
}

package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.ClasePendienteClasificacionResponse;
import com.afazio.dashboard.core.api.ActualizacionClasificacionMasivaResponse;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.Curso;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import com.afazio.dashboard.core.infrastructure.CursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActualizarClasificacionClaseService {

  private final ClaseRepository claseRepository;
  private final ConsultoraRepository consultoraRepository;
  private final CursoRepository cursoRepository;

  public ActualizarClasificacionClaseService(
    ClaseRepository claseRepository,
    ConsultoraRepository consultoraRepository,
    CursoRepository cursoRepository
  ) {
    this.claseRepository = claseRepository;
    this.consultoraRepository = consultoraRepository;
    this.cursoRepository = cursoRepository;
  }

  @Transactional
  public ClasePendienteClasificacionResponse ejecutar(Long claseId, ActualizarClasificacionClaseCommand command) {
    Clase clase = claseRepository.findById(claseId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la clase con el id " + claseId));

    aplicarClasificacion(clase, command);

    Clase saved = claseRepository.save(clase);

    return toResponse(saved);
  }

  @Transactional
  public ActualizacionClasificacionMasivaResponse ejecutarMismoTitulo(Long claseId, ActualizarClasificacionClaseCommand command) {
    Clase claseBase = claseRepository.findById(claseId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la clase con el id " + claseId));

    String titulo = claseBase.getTitulo();
    var clases = buscarClasesRelacionadas(claseBase);

    if (clases.isEmpty()) {
      throw new IllegalArgumentException("No hay clases pendientes relacionadas con la clase " + claseId);
    }

    for (Clase clase : clases) {
      aplicarClasificacion(clase, command);
    }

    claseRepository.saveAll(clases);

    Clase first = clases.getFirst();
    return new ActualizacionClasificacionMasivaResponse(
      titulo,
      clases.size(),
      first.getCurso() != null ? first.getCurso().getId() : null,
      first.getConsultora() != null ? first.getConsultora().getNombre() : null,
      first.getEmpresa(),
      first.getGrupo(),
      first.isFacturable(),
      first.isClasificacionConfirmada()
    );
  }

  private java.util.List<Clase> buscarClasesRelacionadas(Clase claseBase) {
    String serieGoogle = extractSerieGoogle(claseBase.getGoogleEventId());
    if (serieGoogle != null) {
      return claseRepository.findByGoogleEventIdStartingWithAndClasificacionConfirmadaFalseOrderByFechaInicioAsc(serieGoogle + "_");
    }

    return claseRepository.findByTituloAndClasificacionConfirmadaFalseOrderByFechaInicioAsc(claseBase.getTitulo());
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

  private void aplicarClasificacion(Clase clase, ActualizarClasificacionClaseCommand command) {
    if (command.cursoId() != null) {
      Curso curso = cursoRepository.findById(command.cursoId())
        .orElseThrow(() -> new IllegalArgumentException("No existe el curso con el id " + command.cursoId()));

      clase.setCurso(curso);
      clase.setConsultora(curso.getConsultora());
      clase.setEmpresa(curso.getEmpresa());
      clase.setGrupo(curso.getGrupo());
    } else {
      if (command.consultoraId() == null) {
        throw new IllegalArgumentException("La consultora es obligatoria cuando no se informa cursoId");
      }

      Consultora consultora = consultoraRepository.findById(command.consultoraId())
        .orElseThrow(() -> new IllegalArgumentException("No existe la consultora con el id " + command.consultoraId()));

      clase.setCurso(null);
      clase.setConsultora(consultora);
      clase.setEmpresa(command.empresa());
      clase.setGrupo(command.grupo());
    }

    clase.setFacturable(command.facturable());
    clase.setClasificacionConfirmada(command.clasificacionConfirmada());
  }

  private ClasePendienteClasificacionResponse toResponse(Clase clase) {
    return new ClasePendienteClasificacionResponse(
      clase.getId(),
      clase.getCurso() != null ? clase.getCurso().getId() : null,
      clase.getTitulo(),
      clase.getDescripcion(),
      clase.getMeetingUrl(),
      clase.getEmpresa(),
      clase.getGrupo(),
      clase.isFacturable(),
      clase.isClasificacionConfirmada(),
      clase.getConsultora() != null ? clase.getConsultora().getNombre() : null,
      clase.getFechaInicio(),
      clase.getFechaFin(),
      clase.getDuracionMinutos(),
      clase.getEstado(),
      clase.getGoogleEventId()
    );
  }
}

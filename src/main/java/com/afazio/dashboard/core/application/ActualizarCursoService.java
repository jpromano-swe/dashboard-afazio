package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.CursoResponse;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.Curso;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import com.afazio.dashboard.core.infrastructure.CursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActualizarCursoService {

  private final CursoRepository cursoRepository;
  private final ConsultoraRepository consultoraRepository;
  private final ClaseRepository claseRepository;

  public ActualizarCursoService(
    CursoRepository cursoRepository,
    ConsultoraRepository consultoraRepository,
    ClaseRepository claseRepository
  ) {
    this.cursoRepository = cursoRepository;
    this.consultoraRepository = consultoraRepository;
    this.claseRepository = claseRepository;
  }

  @Transactional
  public CursoResponse ejecutar(Long cursoId, ActualizarCursoCommand command) {
    Curso curso = cursoRepository.findById(cursoId)
      .orElseThrow(() -> new IllegalArgumentException("No existe el curso con el id " + cursoId));

    if (command.consultoraId() == null) {
      throw new IllegalArgumentException("La consultora es obligatoria");
    }

    if (command.empresa() == null || command.empresa().isBlank()) {
      throw new IllegalArgumentException("La empresa es obligatoria");
    }

    Consultora consultora = consultoraRepository.findById(command.consultoraId())
      .orElseThrow(() -> new IllegalArgumentException(
        "No existe la consultora con el id " + command.consultoraId()
      ));

    String empresa = command.empresa().trim();
    String grupo = normalize(command.grupo());

    cursoRepository.findByConsultoraAndEmpresaAndGrupoAndIdNot(consultora, empresa, grupo, cursoId)
      .ifPresent(existing -> {
        throw new IllegalArgumentException("Ya existe un curso con esa consultora, empresa y grupo");
      });

    curso.setConsultora(consultora);
    curso.setEmpresa(empresa);
    curso.setGrupo(grupo);
    curso.setActiva(command.activa() == null || command.activa());

    Curso saved = cursoRepository.save(curso);
    actualizarClasesDelCurso(saved);

    return CrearCursoService.toResponse(saved);
  }

  private void actualizarClasesDelCurso(Curso curso) {
    List<Clase> clases = claseRepository.findByCursoOrderByFechaInicioAsc(curso);
    if (clases.isEmpty()) {
      return;
    }

    for (Clase clase : clases) {
      clase.setConsultora(curso.getConsultora());
      clase.setEmpresa(curso.getEmpresa());
      clase.setGrupo(curso.getGrupo());
    }

    claseRepository.saveAll(clases);
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}

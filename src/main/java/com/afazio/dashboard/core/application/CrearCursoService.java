package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.CursoResponse;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.Curso;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import com.afazio.dashboard.core.infrastructure.CursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearCursoService {

  private final CursoRepository cursoRepository;
  private final ConsultoraRepository consultoraRepository;

  public CrearCursoService(
    CursoRepository cursoRepository,
    ConsultoraRepository consultoraRepository
  ) {
    this.cursoRepository = cursoRepository;
    this.consultoraRepository = consultoraRepository;
  }

  @Transactional
  public CursoResponse ejecutar(CrearCursoCommand command) {
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

    cursoRepository.findByConsultoraAndEmpresaAndGrupo(consultora, empresa, grupo)
      .ifPresent(existing -> {
        throw new IllegalArgumentException("Ya existe un curso con esa consultora, empresa y grupo");
      });

    Curso curso = new Curso();
    curso.setConsultora(consultora);
    curso.setEmpresa(empresa);
    curso.setGrupo(grupo);
    curso.setActiva(command.activa() == null || command.activa());

    Curso saved = cursoRepository.save(curso);
    return toResponse(saved);
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  static CursoResponse toResponse(Curso curso) {
    return new CursoResponse(
      curso.getId(),
      curso.getConsultora().getId(),
      curso.getConsultora().getNombre(),
      curso.getEmpresa(),
      curso.getGrupo(),
      curso.isActiva()
    );
  }
}

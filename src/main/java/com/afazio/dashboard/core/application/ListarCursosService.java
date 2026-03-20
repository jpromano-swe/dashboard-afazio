package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.CursoResponse;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import com.afazio.dashboard.core.infrastructure.CursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarCursosService {

  private final CursoRepository cursoRepository;
  private final ConsultoraRepository consultoraRepository;

  public ListarCursosService(
    CursoRepository cursoRepository,
    ConsultoraRepository consultoraRepository
  ) {
    this.cursoRepository = cursoRepository;
    this.consultoraRepository = consultoraRepository;
  }

  @Transactional(readOnly = true)
  public List<CursoResponse> ejecutar(Long consultoraId) {
    List<com.afazio.dashboard.core.domain.Curso> cursos;

    if (consultoraId == null) {
      cursos = cursoRepository.findAllByOrderByEmpresaAscGrupoAsc();
    } else {
      Consultora consultora = consultoraRepository.findById(consultoraId)
        .orElseThrow(() -> new IllegalArgumentException("No existe la consultora con el id " + consultoraId));
      cursos = cursoRepository.findByConsultoraOrderByEmpresaAscGrupoAsc(consultora);
    }

    return cursos.stream()
      .map(CrearCursoService::toResponse)
      .toList();
  }
}

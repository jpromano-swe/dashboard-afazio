package com.afazio.dashboard.core.infrastructure;

import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {
  List<Curso> findAllByOrderByEmpresaAscGrupoAsc();
  List<Curso> findByConsultoraOrderByEmpresaAscGrupoAsc(Consultora consultora);
  Optional<Curso> findByConsultoraAndEmpresaAndGrupo(Consultora consultora, String empresa, String grupo);
}

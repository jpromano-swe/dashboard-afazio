package com.afazio.dashboard.drive.infrastructure;

import com.afazio.dashboard.core.domain.Curso;
import com.afazio.dashboard.drive.domain.TeacherNoteDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherNoteDocumentRepository extends JpaRepository<TeacherNoteDocument, Long> {
  Optional<TeacherNoteDocument> findByCursoAndPeriodo(Curso curso, String periodo);
  List<TeacherNoteDocument> findByPeriodoOrderByCursoEmpresaAscCursoGrupoAsc(String periodo);
  List<TeacherNoteDocument> findByPeriodoAndCursoIdInOrderByCursoEmpresaAscCursoGrupoAsc(String periodo, List<Long> cursoIds);
}

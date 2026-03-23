package com.afazio.dashboard.core.infrastructure;

import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ClaseRepository extends JpaRepository<Clase, Long> {
  List<Clase> findByFechaInicioBetweenOrderByFechaInicioAsc(OffsetDateTime desde, OffsetDateTime hasta);
  Optional<Clase> findByGoogleEventId(String googleEventId);
  List<Clase> findByClasificacionConfirmadaFalseOrderByFechaInicioAsc();
  List<Clase> findByTituloAndClasificacionConfirmadaFalseOrderByFechaInicioAsc(String titulo);
  List<Clase> findByGoogleEventIdStartingWithAndClasificacionConfirmadaFalseOrderByFechaInicioAsc(String googleEventIdPrefix);
  List<Clase> findByCursoOrderByFechaInicioAsc(Curso curso);
  Optional<Clase> findFirstByConsultoraAndGoogleEventIdStartingWithAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(
    Consultora consultora,
    String googleEventIdPrefix
  );
  Optional<Clase> findFirstByGoogleEventIdStartingWithAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(
    String googleEventIdPrefix
  );
  Optional<Clase> findFirstByConsultoraAndTituloAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(
    Consultora consultora,
    String titulo
  );
  Optional<Clase> findFirstByTituloAndClasificacionConfirmadaTrueOrderByFechaInicioDesc(String titulo);
}

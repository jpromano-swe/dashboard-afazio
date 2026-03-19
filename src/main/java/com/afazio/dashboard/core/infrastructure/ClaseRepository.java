package com.afazio.dashboard.core.infrastructure;

import com.afazio.dashboard.core.domain.Clase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ClaseRepository extends JpaRepository<Clase, Long> {
  List<Clase> findByFechaInicioBetweenOrderByFechaInicioAsc(OffsetDateTime desde, OffsetDateTime hasta);
  Optional<Clase> findByGoogleEventId(String googleEventId);
}

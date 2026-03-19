package com.afazio.dashboard.core.infrastructure;

import com.afazio.dashboard.core.domain.Asistencia;
import com.afazio.dashboard.core.domain.Clase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
  Optional<Asistencia> findByClase(Clase clase);
}

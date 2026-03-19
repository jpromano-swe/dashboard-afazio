package com.afazio.dashboard.core.infrastructure;

import com.afazio.dashboard.core.domain.Consultora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultoraRepository extends JpaRepository<Consultora, Long> {
  Optional<Consultora> findByNombre(String nombre);
}

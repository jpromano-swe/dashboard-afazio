package com.afazio.dashboard.core.infrastructure;

import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TarifaConsultoraRepository extends JpaRepository<TarifaConsultora, Long> {
  List<TarifaConsultora> findByConsultoraOrderByVigenteDesdeDesc(Consultora consultora);

  List<TarifaConsultora> findByConsultoraAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
    Consultora consultora,
    LocalDate fecha
  );
}

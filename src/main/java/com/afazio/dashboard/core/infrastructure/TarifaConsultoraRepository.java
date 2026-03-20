package com.afazio.dashboard.core.infrastructure;

import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TarifaConsultoraRepository extends JpaRepository<TarifaConsultora, Long> {

  List<TarifaConsultora> findByConsultoraOrderByVigenteDesdeDesc(Consultora consultora);

  @Query("""
            select t
            from TarifaConsultora t
            where t.consultora = :consultora
              and t.vigenteDesde <= :fecha
              and (t.vigenteHasta is null or t.vigenteHasta >= :fecha)
            order by t.vigenteDesde desc, t.id desc
            """)
  List<TarifaConsultora> findVigentesByConsultoraAndFecha(Consultora consultora, LocalDate fecha);
}

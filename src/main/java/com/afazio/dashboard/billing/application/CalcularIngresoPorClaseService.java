package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.IngresoPorClaseResponse;
import com.afazio.dashboard.core.domain.Asistencia;
import com.afazio.dashboard.core.domain.AsistenciaEstado;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.ClaseEstado;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import com.afazio.dashboard.core.infrastructure.AsistenciaRepository;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class CalcularIngresoPorClaseService {

  private final ClaseRepository claseRepository;
  private final AsistenciaRepository asistenciaRepository;
  private final ObtenerTarifaVigenteService obtenerTarifaVigenteService;

  public CalcularIngresoPorClaseService(
    ClaseRepository claseRepository,
    AsistenciaRepository asistenciaRepository,
    ObtenerTarifaVigenteService obtenerTarifaVigenteService
  ) {
    this.claseRepository = claseRepository;
    this.asistenciaRepository = asistenciaRepository;
    this.obtenerTarifaVigenteService = obtenerTarifaVigenteService;
  }

  @Transactional(readOnly = true)
  public IngresoPorClaseResponse ejecutar(Long claseId) {
    Clase clase = claseRepository.findById(claseId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la clase con el id " + claseId));

    if (clase.getEstado() == ClaseEstado.CANCELADA) {
      throw new IllegalArgumentException("La clase está cancelada y no genera ingresos");
    }

    Asistencia asistencia = asistenciaRepository.findByClase(clase)
      .orElseThrow(() -> new IllegalArgumentException("La clase no tiene asistencia registrada"));

    if (asistencia.getEstado() != AsistenciaEstado.ASISTIO) {
      throw new IllegalArgumentException("La clase no está marcada como ASISTIO y no genera ingresos");
    }

    LocalDate fechaClase = clase.getFechaInicio().toLocalDate();

    TarifaConsultora tarifa = obtenerTarifaVigenteService.ejecutar(clase.getConsultora(), fechaClase);

    BigDecimal minutos = BigDecimal.valueOf(clase.getDuracionMinutos());
    BigDecimal sesenta = BigDecimal.valueOf(60);

    BigDecimal importeCalculado = tarifa.getMontoPorHora()
      .multiply(minutos)
      .divide(sesenta, 2, RoundingMode.HALF_UP);

    return new IngresoPorClaseResponse(
      clase.getId(),
      clase.getConsultora().getNombre(),
      clase.getTitulo(),
      fechaClase,
      clase.getDuracionMinutos(),
      tarifa.getMontoPorHora(),
      tarifa.getMoneda(),
      importeCalculado
    );
  }
}

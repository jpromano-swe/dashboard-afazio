package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.IngresoPorClaseResponse;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.ClaseEstado;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class CalcularIngresoPorClaseService {

  private final ClaseRepository claseRepository;
  private final ObtenerTarifaVigenteService obtenerTarifaVigenteService;

  public CalcularIngresoPorClaseService(
    ClaseRepository claseRepository,
    ObtenerTarifaVigenteService obtenerTarifaVigenteService
  ) {
    this.claseRepository = claseRepository;
    this.obtenerTarifaVigenteService = obtenerTarifaVigenteService;
  }

  @Transactional(readOnly = true)
  public IngresoPorClaseResponse ejecutar(Long claseId) {
    Clase clase = claseRepository.findById(claseId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la clase con el id " + claseId));

    if (clase.getEstado() != ClaseEstado.DICTADA) {
      throw new IllegalArgumentException("La clase debe estar DICTADA para generar ingresos");
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

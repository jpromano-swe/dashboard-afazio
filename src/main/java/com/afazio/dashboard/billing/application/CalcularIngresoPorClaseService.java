package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.IngresoPorClaseResponse;
import com.afazio.dashboard.core.application.ClaseDisplayNames;
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
    String consultoraNombreVisible = ClaseDisplayNames.consultoraNombreVisible(clase);
    boolean sinClasificar = ClaseDisplayNames.esNombreSinClasificar(consultoraNombreVisible);
    TarifaConsultora tarifa = sinClasificar
      ? null
      : obtenerTarifaVigenteService.ejecutar(clase.getConsultora(), fechaClase);

    BigDecimal montoPorHora = tarifa != null ? tarifa.getMontoPorHora() : BigDecimal.ZERO;
    String moneda = tarifa != null ? tarifa.getMoneda() : "ARS";
    BigDecimal importeCalculado = tarifa != null
      ? tarifa.getMontoPorHora()
      .multiply(BigDecimal.valueOf(clase.getDuracionMinutos()))
      .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
      : BigDecimal.ZERO;

    return new IngresoPorClaseResponse(
      clase.getId(),
      clase.getCurso() != null ? clase.getCurso().getId() : null,
      clase.getEmpresa(),
      clase.getGrupo(),
      consultoraNombreVisible,
      clase.getTitulo(),
      fechaClase,
      clase.getDuracionMinutos(),
      montoPorHora,
      moneda,
      clase.isFacturable(),
      sinClasificar,
      importeCalculado
    );
  }
}

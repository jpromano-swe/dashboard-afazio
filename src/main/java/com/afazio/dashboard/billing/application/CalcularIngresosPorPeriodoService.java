package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.IngresoDetalleResponse;
import com.afazio.dashboard.billing.api.IngresoPeriodoResponse;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalcularIngresosPorPeriodoService {

  private final ClaseRepository claseRepository;
  private final AsistenciaRepository asistenciaRepository;
  private final ObtenerTarifaVigenteService obtenerTarifaVigenteService;

  public CalcularIngresosPorPeriodoService(
    ClaseRepository claseRepository,
    AsistenciaRepository asistenciaRepository,
    ObtenerTarifaVigenteService obtenerTarifaVigenteService
  ) {
    this.claseRepository = claseRepository;
    this.asistenciaRepository = asistenciaRepository;
    this.obtenerTarifaVigenteService = obtenerTarifaVigenteService;
  }

  @Transactional(readOnly = true)
  public IngresoPeriodoResponse ejecutar(LocalDate from, LocalDate to) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("Las fechas from y to son obligatorias");
    }

    if (to.isBefore(from)) {
      throw new IllegalArgumentException("La fecha to no puede ser anterior a from");
    }

    OffsetDateTime fromDateTime = from.atStartOfDay().atOffset(ZoneOffset.ofHours(-3));
    OffsetDateTime toDateTime = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.ofHours(-3)).minusNanos(1);

    List<Clase> clases = claseRepository.findByFechaInicioBetweenOrderByFechaInicioAsc(fromDateTime, toDateTime);

    List<IngresoDetalleResponse> detalle = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (Clase clase : clases) {
      if (clase.getEstado() == ClaseEstado.CANCELADA) {
        continue;
      }

      Asistencia asistencia = asistenciaRepository.findByClase(clase).orElse(null);

      if (asistencia == null || asistencia.getEstado() != AsistenciaEstado.ASISTIO) {
        continue;
      }

      LocalDate fechaClase = clase.getFechaInicio().toLocalDate();
      TarifaConsultora tarifa = obtenerTarifaVigenteService.ejecutar(clase.getConsultora(), fechaClase);

      BigDecimal importe = tarifa.getMontoPorHora()
        .multiply(BigDecimal.valueOf(clase.getDuracionMinutos()))
        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

      detalle.add(new IngresoDetalleResponse(
        clase.getId(),
        clase.getConsultora().getNombre(),
        clase.getTitulo(),
        fechaClase,
        clase.getDuracionMinutos(),
        tarifa.getMontoPorHora(),
        tarifa.getMoneda(),
        importe
      ));

      total = total.add(importe);
    }

    return new IngresoPeriodoResponse(
      from,
      to,
      detalle.size(),
      total,
      detalle
    );
  }
}

package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.IngresoDetalleResponse;
import com.afazio.dashboard.billing.api.IngresoPeriodoResponse;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalcularIngresosPorPeriodoService {

  private final ClaseRepository claseRepository;
  private final ObtenerTarifaVigenteService obtenerTarifaVigenteService;

  public CalcularIngresosPorPeriodoService(
    ClaseRepository claseRepository,
    ObtenerTarifaVigenteService obtenerTarifaVigenteService
  ) {
    this.claseRepository = claseRepository;
    this.obtenerTarifaVigenteService = obtenerTarifaVigenteService;
  }

  @Transactional(readOnly = true)
  public IngresoPeriodoResponse ejecutar(LocalDate from, LocalDate to, Long consultoraId, Long cursoId) {
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
      if (clase.getEstado() != ClaseEstado.DICTADA) {
        continue;
      }

      if (!clase.isFacturable()) {
        continue;
      }

      if (consultoraId != null && !clase.getConsultora().getId().equals(consultoraId)) {
        continue;
      }

      if (cursoId != null) {
        if (clase.getCurso() == null || !clase.getCurso().getId().equals(cursoId)) {
          continue;
        }
      }

      LocalDate fechaClase = clase.getFechaInicio().toLocalDate();
      TarifaConsultora tarifa = obtenerTarifaVigenteService.ejecutar(clase.getConsultora(), fechaClase);

      BigDecimal importe = tarifa.getMontoPorHora()
        .multiply(BigDecimal.valueOf(clase.getDuracionMinutos()))
        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

      detalle.add(new IngresoDetalleResponse(
        clase.getId(),
        clase.getCurso() != null ? clase.getCurso().getId() : null,
        clase.getEmpresa(),
        clase.getGrupo(),
        ClaseDisplayNames.consultoraNombreVisible(clase),
        clase.getTitulo(),
        fechaClase,
        clase.getDuracionMinutos(),
        tarifa.getMontoPorHora(),
        tarifa.getMoneda(),
        clase.isFacturable(),
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

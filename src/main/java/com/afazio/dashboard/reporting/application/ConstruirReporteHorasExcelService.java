package com.afazio.dashboard.reporting.application;

import com.afazio.dashboard.billing.application.ObtenerTarifaVigenteService;
import com.afazio.dashboard.core.application.ClaseDisplayNames;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.ClaseEstado;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConstruirReporteHorasExcelService {

  private static final DateTimeFormatter HORARIO_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  private final ConsultoraRepository consultoraRepository;
  private final ClaseRepository claseRepository;
  private final ObtenerTarifaVigenteService obtenerTarifaVigenteService;
  private final TeacherProperties teacherProperties;

  public ConstruirReporteHorasExcelService(
    ConsultoraRepository consultoraRepository,
    ClaseRepository claseRepository,
    ObtenerTarifaVigenteService obtenerTarifaVigenteService,
    TeacherProperties teacherProperties
  ) {
    this.consultoraRepository = consultoraRepository;
    this.claseRepository = claseRepository;
    this.obtenerTarifaVigenteService = obtenerTarifaVigenteService;
    this.teacherProperties = teacherProperties;
  }

  @Transactional(readOnly = true)
  public ReporteHorasExcelData ejecutar(Long consultoraId, YearMonth periodo) {
    Consultora consultora = consultoraRepository.findById(consultoraId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la consultora con el id " + consultoraId));

    OffsetDateTime from = periodo.atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(-3));
    OffsetDateTime to = periodo.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.ofHours(-3)).minusNanos(1);

    List<Clase> clases = claseRepository.findByFechaInicioBetweenOrderByFechaInicioAsc(from, to).stream()
      .filter(clase -> clase.getConsultora().getId().equals(consultoraId))
      .filter(clase -> clase.getEstado() == ClaseEstado.DICTADA)
      .toList();

    Map<String, AcumuladorFila> agrupado = new LinkedHashMap<>();

    for (Clase clase : clases) {
      String consultoraNombreVisible = ClaseDisplayNames.consultoraNombreVisible(clase);
      boolean sinClasificar = ClaseDisplayNames.esNombreSinClasificar(consultoraNombreVisible);
      TarifaConsultora tarifa = sinClasificar
        ? null
        : obtenerTarifaVigenteService.ejecutar(
          consultora,
          clase.getFechaInicio().toLocalDate()
        );

      BigDecimal duracionHoras = BigDecimal.valueOf(clase.getDuracionMinutos())
        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

      String horario = clase.getFechaInicio().toLocalTime().format(HORARIO_FORMATTER)
        + " a "
        + clase.getFechaFin().toLocalTime().format(HORARIO_FORMATTER);

      String empresa = clase.getEmpresa() != null && !clase.getEmpresa().isBlank()
        ? clase.getEmpresa()
        : consultora.getNombre();
      String grupo = clase.getGrupo() != null && !clase.getGrupo().isBlank()
        ? clase.getGrupo()
        : "";

      BigDecimal honorarios = tarifa != null ? tarifa.getMontoPorHora() : BigDecimal.ZERO;
      String moneda = tarifa != null ? tarifa.getMoneda() : "ARS";

      String key = empresa + "|" + grupo + "|" + horario + "|" + honorarios;

      AcumuladorFila acumulador = agrupado.computeIfAbsent(
        key,
        ignored -> new AcumuladorFila(
          empresa,
          grupo,
          horario,
          duracionHoras,
          honorarios,
          moneda
        )
      );

      acumulador.incrementarClases();
    }

    List<ReporteHorasExcelRow> rows = agrupado.values().stream()
      .map(AcumuladorFila::toRow)
      .toList();

    if (rows.size() > 9) {
      throw new IllegalArgumentException("El reporte supera el máximo de 9 filas del template");
    }

    BigDecimal total = rows.stream()
      .map(ReporteHorasExcelRow::subtotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new ReporteHorasExcelData(
      teacherProperties.name(),
      teacherProperties.cuit(),
      consultora.getNombre(),
      periodo,
      rows,
      total
    );
  }

  private static class AcumuladorFila {
    private final String empresa;
    private final String grupo;
    private final String horario;
    private final BigDecimal duracionClaseHoras;
    private final BigDecimal honorarios;
    private final String moneda;
    private int clases;

    private AcumuladorFila(
      String empresa,
      String grupo,
      String horario,
      BigDecimal duracionClaseHoras,
      BigDecimal honorarios,
      String moneda
    ) {
      this.empresa = empresa;
      this.grupo = grupo;
      this.horario = horario;
      this.duracionClaseHoras = duracionClaseHoras;
      this.honorarios = honorarios;
      this.moneda = moneda;
    }

    private void incrementarClases() {
      this.clases++;
    }

    private ReporteHorasExcelRow toRow() {
      BigDecimal cantidadTotalHoras = duracionClaseHoras.multiply(BigDecimal.valueOf(clases));
      BigDecimal subtotal = cantidadTotalHoras.multiply(honorarios);

      return new ReporteHorasExcelRow(
        empresa,
        grupo,
        horario,
        clases,
        duracionClaseHoras,
        cantidadTotalHoras,
        honorarios,
        subtotal
      );
    }
  }
}

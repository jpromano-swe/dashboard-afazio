package com.afazio.dashboard.reporting.api;

import com.afazio.dashboard.reporting.application.GenerarReporteHorasExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reportes")
public class ReporteExcelController {

  private final GenerarReporteHorasExcelService generarReporteHorasExcelService;

  public ReporteExcelController(GenerarReporteHorasExcelService generarReporteHorasExcelService) {
    this.generarReporteHorasExcelService = generarReporteHorasExcelService;
  }

  @GetMapping("/excel")
  public ResponseEntity<byte[]> descargarExcel(
    @RequestParam Long consultoraId,
    @RequestParam YearMonth periodo
  ) {
    byte[] file = generarReporteHorasExcelService.ejecutar(consultoraId, periodo);

    String fileName = "reporte-horas-" + consultoraId + "-" + periodo + ".xlsx";

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
      .contentType(MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      ))
      .body(file);
  }
}

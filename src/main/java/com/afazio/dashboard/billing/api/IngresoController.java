package com.afazio.dashboard.billing.api;

import com.afazio.dashboard.billing.application.CalcularIngresoPorClaseService;
import com.afazio.dashboard.billing.application.CalcularIngresosPorPeriodoService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {

  private final CalcularIngresoPorClaseService calcularIngresoPorClaseService;
  private final CalcularIngresosPorPeriodoService calcularIngresosPorPeriodoService;

  public IngresoController(
    CalcularIngresoPorClaseService calcularIngresoPorClaseService,
    CalcularIngresosPorPeriodoService calcularIngresosPorPeriodoService

  ) {
    this.calcularIngresoPorClaseService = calcularIngresoPorClaseService;
    this.calcularIngresosPorPeriodoService = calcularIngresosPorPeriodoService;
  }

  @GetMapping("/clases/{claseId}")
  public IngresoPorClaseResponse calcularPorClase(@PathVariable Long claseId) {
    return calcularIngresoPorClaseService.ejecutar(claseId);
  }

  @GetMapping
  public IngresoPeriodoResponse calcularPorPeriodo(
    @RequestParam LocalDate from,
    @RequestParam LocalDate to
  ) {
    return calcularIngresosPorPeriodoService.ejecutar(from, to);
  }
}

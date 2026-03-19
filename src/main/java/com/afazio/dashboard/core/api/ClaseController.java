package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.application.ListarClasesDelDiaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/clases")
public class ClaseController {

  private final ListarClasesDelDiaService listarClasesDelDiaService;

  public ClaseController(ListarClasesDelDiaService listarClasesDelDiaService) {
    this.listarClasesDelDiaService = listarClasesDelDiaService;
  }

  @GetMapping("/hoy")
  public List<ClaseDelDiaResponse> listarHoy(
    @RequestParam(required = false) LocalDate fecha
  ) {
    LocalDate targetDate = fecha != null ? fecha : LocalDate.now();
    return listarClasesDelDiaService.ejecutar(targetDate);
  }
}

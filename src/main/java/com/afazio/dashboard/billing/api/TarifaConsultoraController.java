package com.afazio.dashboard.billing.api;


import com.afazio.dashboard.billing.application.CrearTarifaConsultoraCommand;
import com.afazio.dashboard.billing.application.ActualizarTarifaConsultoraService;
import com.afazio.dashboard.billing.application.CrearTarifaConsultoraService;
import com.afazio.dashboard.billing.application.ListarTarifasPorConsultoraService;
import com.afazio.dashboard.billing.application.ObtenerTarifaVigentePorConsultoraService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaConsultoraController {
  private final ActualizarTarifaConsultoraService actualizarTarifaConsultoraService;
  private final CrearTarifaConsultoraService crearTarifaConsultoraService;
  private final ListarTarifasPorConsultoraService listarTarifasPorConsultoraService;
  private final ObtenerTarifaVigentePorConsultoraService obtenerTarifaVigentePorConsultoraService;

  public TarifaConsultoraController(
    ActualizarTarifaConsultoraService actualizarTarifaConsultoraService,
    CrearTarifaConsultoraService crearTarifaConsultoraService,
    ListarTarifasPorConsultoraService listarTarifasPorConsultoraService,
    ObtenerTarifaVigentePorConsultoraService obtenerTarifaVigentePorConsultoraService
  ) {
    this.actualizarTarifaConsultoraService = actualizarTarifaConsultoraService;
    this.crearTarifaConsultoraService = crearTarifaConsultoraService;
    this.listarTarifasPorConsultoraService = listarTarifasPorConsultoraService;
    this.obtenerTarifaVigentePorConsultoraService = obtenerTarifaVigentePorConsultoraService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TarifaConsultoraResponse crear(@RequestBody CrearTarifaConsultoraCommand command) {
    return crearTarifaConsultoraService.ejecutar(command);
  }

  @PutMapping("/{id}")
  public TarifaConsultoraResponse actualizar(
    @PathVariable Long id,
    @RequestBody CrearTarifaConsultoraCommand command
  ) {
    return actualizarTarifaConsultoraService.ejecutar(id, command);
  }

  @GetMapping("/consultoras/{consultoraId}")
  public List<TarifaConsultoraResponse> listarPorConsultora(@PathVariable Long consultoraId) {
    return listarTarifasPorConsultoraService.ejecutar(consultoraId);
  }

  @GetMapping("/vigente")
  public TarifaConsultoraResponse obtenerVigente(
    @RequestParam Long consultoraId,
    @RequestParam LocalDate fecha
  ) {
    return obtenerTarifaVigentePorConsultoraService.ejecutar(consultoraId, fecha);
  }
}

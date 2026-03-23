package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.api.ActualizacionClasificacionMasivaResponse;
import com.afazio.dashboard.core.application.ActualizarClasificacionClaseCommand;
import com.afazio.dashboard.core.application.ActualizarClasificacionClaseService;
import com.afazio.dashboard.core.application.ActualizarEstadoClaseCommand;
import com.afazio.dashboard.core.application.ActualizarEstadoClaseService;
import com.afazio.dashboard.core.application.ListarClasesDelDiaService;
import com.afazio.dashboard.core.application.ListarClasesPorPeriodoService;
import com.afazio.dashboard.core.application.ListarClasesPendientesClasificacionService;
import com.afazio.dashboard.core.application.MarcarClaseDictadaService;
import com.afazio.dashboard.core.application.PurgarClasesCommand;
import com.afazio.dashboard.core.application.PurgarClasesService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/clases")
public class ClaseController {

  private final ListarClasesDelDiaService listarClasesDelDiaService;
  private final ListarClasesPorPeriodoService listarClasesPorPeriodoService;
  private final ListarClasesPendientesClasificacionService listarClasesPendientesClasificacionService;
  private final ActualizarClasificacionClaseService actualizarClasificacionClaseService;
  private final ActualizarEstadoClaseService actualizarEstadoClaseService;
  private final MarcarClaseDictadaService marcarClaseDictadaService;
  private final PurgarClasesService purgarClasesService;

  public ClaseController(
    ListarClasesDelDiaService listarClasesDelDiaService,
    ListarClasesPorPeriodoService listarClasesPorPeriodoService,
    ListarClasesPendientesClasificacionService listarClasesPendientesClasificacionService,
    ActualizarClasificacionClaseService actualizarClasificacionClaseService,
    ActualizarEstadoClaseService actualizarEstadoClaseService,
    MarcarClaseDictadaService marcarClaseDictadaService,
    PurgarClasesService purgarClasesService
  ) {
    this.listarClasesDelDiaService = listarClasesDelDiaService;
    this.listarClasesPorPeriodoService = listarClasesPorPeriodoService;
    this.listarClasesPendientesClasificacionService = listarClasesPendientesClasificacionService;
    this.actualizarClasificacionClaseService = actualizarClasificacionClaseService;
    this.actualizarEstadoClaseService = actualizarEstadoClaseService;
    this.marcarClaseDictadaService = marcarClaseDictadaService;
    this.purgarClasesService = purgarClasesService;
  }

  @GetMapping("/hoy")
  public List<ClaseDelDiaResponse> listarHoy(
    @RequestParam(required = false) LocalDate fecha
  ) {
    LocalDate targetDate = fecha != null ? fecha : LocalDate.now();
    return listarClasesDelDiaService.ejecutar(targetDate);
  }

  @GetMapping
  public List<ClaseDelDiaResponse> listarPorPeriodo(
    @RequestParam LocalDate from,
    @RequestParam LocalDate to,
    @RequestParam(required = false, defaultValue = "false") boolean soloClasificadas
  ) {
    return listarClasesPorPeriodoService.ejecutar(from, to, soloClasificadas);
  }

  @GetMapping("/semana")
  public List<ClaseDelDiaResponse> listarSemana(
    @RequestParam LocalDate desde,
    @RequestParam(required = false, defaultValue = "false") boolean soloClasificadas
  ) {
    LocalDate hasta = desde.plusDays(6);
    return listarClasesPorPeriodoService.ejecutar(desde, hasta, soloClasificadas);
  }

  @GetMapping("/pendientes-clasificacion")
  public List<ClasePendienteClasificacionResponse> listarPendientesClasificacion() {
    return listarClasesPendientesClasificacionService.ejecutar();
  }

  @PutMapping("/{id}/clasificacion")
  public ClasePendienteClasificacionResponse actualizarClasificacion(
    @PathVariable Long id,
    @RequestBody ActualizarClasificacionClaseCommand command
  ) {
    return actualizarClasificacionClaseService.ejecutar(id, command);
  }

  @PutMapping("/{id}/clasificacion/mismo-titulo")
  public ActualizacionClasificacionMasivaResponse actualizarClasificacionMismoTitulo(
    @PathVariable Long id,
    @RequestBody ActualizarClasificacionClaseCommand command
  ) {
    return actualizarClasificacionClaseService.ejecutarMismoTitulo(id, command);
  }

  @PutMapping("/{id}/estado")
  public ActualizacionEstadoClaseResponse actualizarEstado(
    @PathVariable Long id,
    @RequestBody ActualizarEstadoClaseCommand command
  ) {
    return actualizarEstadoClaseService.ejecutar(id, command);
  }

  @PutMapping("/{id}/dictada")
  public ActualizacionEstadoClaseResponse marcarDictada(@PathVariable Long id) {
    return marcarClaseDictadaService.ejecutar(id);
  }

  @DeleteMapping("/purga")
  public PurgarClasesResponse purgarPorTitulos(@RequestBody PurgarClasesCommand command) {
    return purgarClasesService.ejecutar(command);
  }

}

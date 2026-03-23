package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.application.CrearCursoCommand;
import com.afazio.dashboard.core.application.ActualizarCursoCommand;
import com.afazio.dashboard.core.application.ActualizarCursoService;
import com.afazio.dashboard.core.application.CrearCursoService;
import com.afazio.dashboard.core.application.ListarCursosService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

  private final ListarCursosService listarCursosService;
  private final CrearCursoService crearCursoService;
  private final ActualizarCursoService actualizarCursoService;

  public CursoController(
    ListarCursosService listarCursosService,
    CrearCursoService crearCursoService,
    ActualizarCursoService actualizarCursoService
  ) {
    this.listarCursosService = listarCursosService;
    this.crearCursoService = crearCursoService;
    this.actualizarCursoService = actualizarCursoService;
  }

  @GetMapping
  public List<CursoResponse> listar(@RequestParam(required = false) Long consultoraId) {
    return listarCursosService.ejecutar(consultoraId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CursoResponse crear(@RequestBody CrearCursoCommand command) {
    return crearCursoService.ejecutar(command);
  }

  @PutMapping("/{id}")
  public CursoResponse actualizar(
    @PathVariable Long id,
    @RequestBody ActualizarCursoCommand command
  ) {
    return actualizarCursoService.ejecutar(id, command);
  }
}

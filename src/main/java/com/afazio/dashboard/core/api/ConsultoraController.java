package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.application.CrearConsultoraCommand;
import com.afazio.dashboard.core.application.CrearConsultoraService;
import com.afazio.dashboard.core.application.EliminarConsultoraService;
import com.afazio.dashboard.core.application.ListarConsultorasService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultoras")
public class ConsultoraController {

  private final CrearConsultoraService crearConsultoraService;
  private final EliminarConsultoraService eliminarConsultoraService;
  private final ListarConsultorasService listarConsultorasService;

  public ConsultoraController(
    CrearConsultoraService crearConsultoraService,
    EliminarConsultoraService eliminarConsultoraService,
    ListarConsultorasService listarConsultorasService
  ) {
    this.crearConsultoraService = crearConsultoraService;
    this.eliminarConsultoraService = eliminarConsultoraService;
    this.listarConsultorasService = listarConsultorasService;
  }

  @GetMapping
  public List<ConsultoraResponse> listar() {
    return listarConsultorasService.ejecutar();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ConsultoraResponse crear(@RequestBody CrearConsultoraCommand command){
    return crearConsultoraService.ejecutar(command);
  }

  @DeleteMapping("/{consultoraId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long consultoraId) {
    eliminarConsultoraService.ejecutar(consultoraId);
  }
}

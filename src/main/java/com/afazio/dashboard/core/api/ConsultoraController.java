package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.application.CrearConsultoraCommand;
import com.afazio.dashboard.core.application.CrearConsultoraService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultoras")
public class ConsultoraController {

  private final CrearConsultoraService crearConsultoraService;

  public ConsultoraController(CrearConsultoraService crearConsultoraService) {
    this.crearConsultoraService = crearConsultoraService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ConsultoraResponse crear(@RequestBody CrearConsultoraCommand command){
    return crearConsultoraService.ejecutar(command);
  }
}

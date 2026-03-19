package com.afazio.dashboard.core.api;

import com.afazio.dashboard.core.application.MarcarAsistenciaCommand;
import com.afazio.dashboard.core.application.MarcarAsistenciaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

  private final MarcarAsistenciaService marcarAsistenciaService;

  public AsistenciaController(MarcarAsistenciaService marcarAsistenciaService) {
    this.marcarAsistenciaService = marcarAsistenciaService;
  }

  @PostMapping
  public AsistenciaResponse marcar(@RequestBody MarcarAsistenciaCommand command){
    return marcarAsistenciaService.ejecutar(command);
  }
}

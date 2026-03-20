package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.ActualizacionEstadoClaseResponse;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.ClaseEstado;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarcarClaseDictadaService {

  private final ClaseRepository claseRepository;

  public MarcarClaseDictadaService(ClaseRepository claseRepository) {
    this.claseRepository = claseRepository;
  }

  @Transactional
  public ActualizacionEstadoClaseResponse ejecutar(Long claseId) {
    Clase clase = claseRepository.findById(claseId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la clase con el id " + claseId));

    if (clase.getEstado() != ClaseEstado.PROGRAMADA) {
      throw new IllegalArgumentException("Solo se puede marcar como DICTADA una clase que esté PROGRAMADA");
    }

    ClaseEstado estadoAnterior = clase.getEstado();
    clase.setEstado(ClaseEstado.DICTADA);

    Clase saved = claseRepository.save(clase);

    return new ActualizacionEstadoClaseResponse(
      saved.getId(),
      saved.getTitulo(),
      estadoAnterior,
      saved.getEstado()
    );
  }
}

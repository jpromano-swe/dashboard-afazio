package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.ActualizacionEstadoClaseResponse;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.ClaseEstado;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
public class ActualizarEstadoClaseService {

  private static final EnumSet<ClaseEstado> ESTADOS_DESTINO_VALIDOS = EnumSet.of(
    ClaseEstado.DICTADA,
    ClaseEstado.CANCELADA,
    ClaseEstado.REPROGRAMADA
  );

  private final ClaseRepository claseRepository;

  public ActualizarEstadoClaseService(ClaseRepository claseRepository) {
    this.claseRepository = claseRepository;
  }

  @Transactional
  public ActualizacionEstadoClaseResponse ejecutar(Long claseId, ActualizarEstadoClaseCommand command) {
    Clase clase = claseRepository.findById(claseId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la clase con el id " + claseId));

    if (command.estado() == null) {
      throw new IllegalArgumentException("El estado es obligatorio");
    }

    if (!ESTADOS_DESTINO_VALIDOS.contains(command.estado())) {
      throw new IllegalArgumentException("Solo se permite cambiar a DICTADA, CANCELADA o REPROGRAMADA");
    }

    if (clase.getEstado() != ClaseEstado.PROGRAMADA) {
      throw new IllegalArgumentException(
        "Solo se puede cambiar el estado de una clase que esté PROGRAMADA"
      );
    }

    ClaseEstado estadoAnterior = clase.getEstado();
    clase.setEstado(command.estado());

    Clase saved = claseRepository.save(clase);

    return new ActualizacionEstadoClaseResponse(
      saved.getId(),
      saved.getTitulo(),
      estadoAnterior,
      saved.getEstado()
    );
  }
}

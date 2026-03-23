package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarConsultoraService {

  private final ConsultoraRepository consultoraRepository;

  public EliminarConsultoraService(ConsultoraRepository consultoraRepository) {
    this.consultoraRepository = consultoraRepository;
  }

  @Transactional
  public void ejecutar(Long consultoraId) {
    var consultora = consultoraRepository.findById(consultoraId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la consultora con el id " + consultoraId));

    consultora.setActiva(false);
    consultoraRepository.save(consultora);
  }
}

package com.afazio.dashboard.billing.application;

import com.afazio.dashboard.billing.api.TarifaConsultoraResponse;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.TarifaConsultora;
import com.afazio.dashboard.core.infrastructure.ConsultoraRepository;
import com.afazio.dashboard.core.infrastructure.TarifaConsultoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ActualizarTarifaConsultoraService {

  private final ConsultoraRepository consultoraRepository;
  private final TarifaConsultoraRepository tarifaConsultoraRepository;

  public ActualizarTarifaConsultoraService(
    ConsultoraRepository consultoraRepository,
    TarifaConsultoraRepository tarifaConsultoraRepository
  ) {
    this.consultoraRepository = consultoraRepository;
    this.tarifaConsultoraRepository = tarifaConsultoraRepository;
  }

  @Transactional
  public TarifaConsultoraResponse ejecutar(Long tarifaId, CrearTarifaConsultoraCommand command) {
    TarifaConsultora tarifa = tarifaConsultoraRepository.findById(tarifaId)
      .orElseThrow(() -> new IllegalArgumentException("No existe la tarifa con el id " + tarifaId));

    Consultora consultora = consultoraRepository.findById(command.consultoraId())
      .orElseThrow(() -> new IllegalArgumentException(
        "No existe la consultora con el id " + command.consultoraId()
      ));

    validar(command, consultora, tarifaId);

    tarifa.setConsultora(consultora);
    tarifa.setMontoPorHora(command.montoPorHora());
    tarifa.setMoneda(command.moneda() != null ? command.moneda().toUpperCase(Locale.ROOT) : "ARS");
    tarifa.setVigenteDesde(command.vigenteDesde());
    tarifa.setVigenteHasta(command.vigenteHasta());
    tarifa.setFechaUltimoAumento(command.fechaUltimoAumento());
    tarifa.setObservaciones(command.observaciones());

    TarifaConsultora saved = tarifaConsultoraRepository.save(tarifa);

    return new TarifaConsultoraResponse(
      saved.getId(),
      saved.getConsultora().getId(),
      saved.getConsultora().getNombre(),
      saved.getMontoPorHora(),
      saved.getMoneda(),
      saved.getVigenteDesde(),
      saved.getVigenteHasta(),
      saved.getFechaUltimoAumento(),
      saved.getObservaciones()
    );
  }

  private void validar(CrearTarifaConsultoraCommand command, Consultora consultora, Long tarifaIdAExcluir) {
    if (command.montoPorHora() == null || command.montoPorHora().signum() <= 0) {
      throw new IllegalArgumentException("El monto por hora debe ser mayor a cero");
    }

    if (command.vigenteDesde() == null) {
      throw new IllegalArgumentException("La fecha de vigencia desde es obligatoria");
    }

    if (command.vigenteHasta() != null && command.vigenteHasta().isBefore(command.vigenteDesde())) {
      throw new IllegalArgumentException("La fecha vigenteHasta no puede ser anterior a vigenteDesde");
    }

    boolean hasOverlap = tarifaConsultoraRepository.findByConsultoraOrderByVigenteDesdeDesc(consultora).stream()
      .filter(tarifa -> !tarifa.getId().equals(tarifaIdAExcluir))
      .anyMatch(tarifa -> seSuperpone(
        command.vigenteDesde(),
        command.vigenteHasta(),
        tarifa.getVigenteDesde(),
        tarifa.getVigenteHasta()
      ));

    if (hasOverlap) {
      throw new IllegalArgumentException(
        "Ya existe una tarifa superpuesta para la consultora " + consultora.getNombre()
      );
    }
  }

  private boolean seSuperpone(
    java.time.LocalDate desdeNueva,
    java.time.LocalDate hastaNueva,
    java.time.LocalDate desdeExistente,
    java.time.LocalDate hastaExistente
  ) {
    java.time.LocalDate finNueva = hastaNueva != null ? hastaNueva : java.time.LocalDate.MAX;
    java.time.LocalDate finExistente = hastaExistente != null ? hastaExistente : java.time.LocalDate.MAX;

    return !finNueva.isBefore(desdeExistente) && !finExistente.isBefore(desdeNueva);
  }
}

package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.ClasePendienteClasificacionResponse;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarClasesPendientesClasificacionService {

  private final ClaseRepository claseRepository;

  public ListarClasesPendientesClasificacionService(ClaseRepository claseRepository) {
    this.claseRepository = claseRepository;
  }

  @Transactional(readOnly = true)
  public List<ClasePendienteClasificacionResponse> ejecutar() {
    return claseRepository.findByClasificacionConfirmadaFalseOrderByFechaInicioAsc().stream()
      .map(clase -> new ClasePendienteClasificacionResponse(
        clase.getId(),
        clase.getCurso() != null ? clase.getCurso().getId() : null,
        clase.getTitulo(),
        clase.getDescripcion(),
        clase.getMeetingUrl(),
        clase.getEmpresa(),
        clase.getGrupo(),
        clase.isFacturable(),
        clase.isClasificacionConfirmada(),
        clase.getConsultora() != null ? clase.getConsultora().getNombre() : null,
        clase.getFechaInicio(),
        clase.getFechaFin(),
        clase.getDuracionMinutos(),
        clase.getEstado(),
        clase.getGoogleEventId()
      ))
      .toList();
  }
}

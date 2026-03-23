package com.afazio.dashboard.core.application;

import com.afazio.dashboard.core.api.PurgarClasesResponse;
import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurgarClasesService {

  private final ClaseRepository claseRepository;

  public PurgarClasesService(ClaseRepository claseRepository) {
    this.claseRepository = claseRepository;
  }

  @Transactional
  public PurgarClasesResponse ejecutar(PurgarClasesCommand command) {
    if (command.titulos() == null || command.titulos().isEmpty()) {
      throw new IllegalArgumentException("Debes informar al menos un titulo");
    }

    Set<String> titulosNormalizados = new LinkedHashSet<>();
    for (String titulo : command.titulos()) {
      if (titulo != null && !titulo.isBlank()) {
        titulosNormalizados.add(normalize(titulo));
      }
    }

    if (titulosNormalizados.isEmpty()) {
      throw new IllegalArgumentException("Debes informar al menos un titulo valido");
    }

    List<Clase> clasesAEliminar = claseRepository.findAll().stream()
      .filter(clase -> titulosNormalizados.contains(normalize(clase.getTitulo())))
      .toList();

    List<Long> ids = clasesAEliminar.stream().map(Clase::getId).toList();
    claseRepository.deleteAll(new ArrayList<>(clasesAEliminar));

    return new PurgarClasesResponse(ids.size(), ids, new ArrayList<>(titulosNormalizados));
  }

  private String normalize(String value) {
    return value.trim().toLowerCase();
  }
}

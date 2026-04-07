package com.afazio.dashboard.drive.api;

import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;
import java.util.List;

public record TeachersNotesGenerateRequest(
  @NotNull YearMonth periodo,
  List<Long> cursoIds
) {
}

package com.afazio.dashboard.core.api;

import java.util.List;

public record PurgarClasesResponse(
  int deleted,
  List<Long> claseIds,
  List<String> titulos
) {
}

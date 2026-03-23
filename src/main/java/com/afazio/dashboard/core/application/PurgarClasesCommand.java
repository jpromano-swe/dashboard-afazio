package com.afazio.dashboard.core.application;

import java.util.List;

public record PurgarClasesCommand(
  List<String> titulos
) {
}

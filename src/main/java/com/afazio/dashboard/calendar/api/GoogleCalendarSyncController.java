package com.afazio.dashboard.calendar.api;

import com.afazio.dashboard.calendar.application.SincronizarClasesDesdeGoogleService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/google/calendar")
public class GoogleCalendarSyncController {

  private final SincronizarClasesDesdeGoogleService sincronizarClasesDesdeGoogleService;

  public GoogleCalendarSyncController(SincronizarClasesDesdeGoogleService sincronizarClasesDesdeGoogleService) {
    this.sincronizarClasesDesdeGoogleService = sincronizarClasesDesdeGoogleService;
  }

  @GetMapping("/sync")
  public Map<String, Object> sync(
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
    @RequestParam OffsetDateTime from,
    @RequestParam OffsetDateTime to
  ) {
    int processed = sincronizarClasesDesdeGoogleService.ejecutar(authorizedClient, from, to);

    return Map.of(
      "processed", processed,
      "from", from,
      "to", to
    );
  }
}

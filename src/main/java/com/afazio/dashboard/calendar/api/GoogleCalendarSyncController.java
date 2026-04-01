package com.afazio.dashboard.calendar.api;

import com.afazio.dashboard.calendar.application.CalendarSyncRangeResolver;
import com.afazio.dashboard.calendar.application.SincronizarClasesDesdeGoogleService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/google/calendar")
public class GoogleCalendarSyncController {

  private final SincronizarClasesDesdeGoogleService sincronizarClasesDesdeGoogleService;
  private final CalendarSyncRangeResolver calendarSyncRangeResolver;

  public GoogleCalendarSyncController(
    SincronizarClasesDesdeGoogleService sincronizarClasesDesdeGoogleService,
    CalendarSyncRangeResolver calendarSyncRangeResolver
  ) {
    this.sincronizarClasesDesdeGoogleService = sincronizarClasesDesdeGoogleService;
    this.calendarSyncRangeResolver = calendarSyncRangeResolver;
  }

  @GetMapping("/sync")
  public Map<String, Object> sync(
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
    @RequestParam(required = false) OffsetDateTime from,
    @RequestParam(required = false) OffsetDateTime to,
    @RequestParam(required = false) LocalDate fromDate,
    @RequestParam(required = false) LocalDate toDate
  ) {
    var range = calendarSyncRangeResolver.resolve(from, to, fromDate, toDate);
    int processed = sincronizarClasesDesdeGoogleService.ejecutar(authorizedClient, range.from(), range.to());

    return Map.of(
      "processed", processed,
      "from", range.from(),
      "to", range.to()
    );
  }
}

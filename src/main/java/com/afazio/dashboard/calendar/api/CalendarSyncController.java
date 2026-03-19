package com.afazio.dashboard.calendar.api;

import com.afazio.dashboard.calendar.application.SincronizarClasesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/calendar")
public class CalendarSyncController {

  private final SincronizarClasesService sincronizarClasesService;

  public CalendarSyncController(SincronizarClasesService sincronizarClasesService) {
    this.sincronizarClasesService = sincronizarClasesService;
  }

  @PostMapping("/sync")
  public SyncCalendarResponse sync(
    @RequestParam OffsetDateTime from,
    @RequestParam OffsetDateTime to
    ){
    int processed = sincronizarClasesService.ejecutar(from, to);
    return new SyncCalendarResponse(processed);
  }
}

package com.afazio.dashboard.calendar.infrastructure;

import com.afazio.dashboard.calendar.application.CalendarClassEvent;
import com.afazio.dashboard.calendar.application.CalendarProvider;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class StubCalendarProvider implements CalendarProvider {

  @Override
  public List<CalendarClassEvent> getClassesBetween(OffsetDateTime from, OffsetDateTime to) {
    return List.of(
      new CalendarClassEvent(
        "google-event-1",
        "Curso A2 Accenture",
        "Clase inicial de prueba",
        null,
        from.plusHours(2),
        from.plusHours(4),
        "Accenture"
      )
    );
  }
}

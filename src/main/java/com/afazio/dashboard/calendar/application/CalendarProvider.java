package com.afazio.dashboard.calendar.application;

import java.time.OffsetDateTime;
import java.util.List;

public interface CalendarProvider {

  List<CalendarClassEvent> getClassesBetween(OffsetDateTime from, OffsetDateTime to);
}

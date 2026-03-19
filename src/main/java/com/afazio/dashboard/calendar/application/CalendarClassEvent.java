package com.afazio.dashboard.calendar.application;

import java.time.OffsetDateTime;

public record CalendarClassEvent(
  String externalEventId,
  String title,
  String description,
  OffsetDateTime startAt,
  OffsetDateTime endAt,
  String consultoraNombre
) {
}

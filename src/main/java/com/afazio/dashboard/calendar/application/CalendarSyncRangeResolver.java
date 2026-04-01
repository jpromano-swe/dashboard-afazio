package com.afazio.dashboard.calendar.application;

import com.afazio.dashboard.shared.config.AppTimeProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Component
public class CalendarSyncRangeResolver {

  private final AppTimeProperties appTimeProperties;

  public CalendarSyncRangeResolver(AppTimeProperties appTimeProperties) {
    this.appTimeProperties = appTimeProperties;
  }

  public TimeRange resolve(
    OffsetDateTime from,
    OffsetDateTime to,
    LocalDate fromDate,
    LocalDate toDate
  ) {
    boolean hasDateRange = fromDate != null || toDate != null;
    boolean hasDateTimeRange = from != null || to != null;

    if (hasDateRange && hasDateTimeRange) {
      throw new IllegalArgumentException("Usa from/to o fromDate/toDate, no ambos");
    }

    if (hasDateRange) {
      if (fromDate == null || toDate == null) {
        throw new IllegalArgumentException("fromDate y toDate son obligatorios");
      }

      return new TimeRange(
        fromDate.atStartOfDay(appTimeProperties.zone()).toOffsetDateTime(),
        toDate.plusDays(1).atStartOfDay(appTimeProperties.zone()).minusNanos(1).toOffsetDateTime()
      );
    }

    if (from == null || to == null) {
      throw new IllegalArgumentException("from y to son obligatorios");
    }

    return new TimeRange(from, to);
  }

  public record TimeRange(
    OffsetDateTime from,
    OffsetDateTime to
  ) {
  }
}

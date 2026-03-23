package com.afazio.dashboard.calendar.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public record GoogleCalendarProperties(
  String calendarId
) {
}

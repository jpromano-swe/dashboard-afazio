package com.afazio.dashboard.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

@ConfigurationProperties(prefix = "app.time")
public record AppTimeProperties(
  String zoneId
) {

  public ZoneId zone() {
    return ZoneId.of(zoneId);
  }
}

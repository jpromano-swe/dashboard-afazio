package com.afazio.dashboard.reporting.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.teacher")
public record TeacherProperties(
  String name,
  String cuit
) {
}

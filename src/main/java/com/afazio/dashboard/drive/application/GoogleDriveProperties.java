package com.afazio.dashboard.drive.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google.drive")
public record GoogleDriveProperties(
  String rootFolderId
) {
}

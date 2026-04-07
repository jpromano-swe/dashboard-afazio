package com.afazio.dashboard.drive.api;

import java.time.OffsetDateTime;

public record TeachersNotesCourseResponse(
  Long cursoId,
  String cursoNombre,
  String periodo,
  String status,
  String message,
  String driveFileId,
  String driveUrl,
  String folderPath,
  String documentTitle,
  int sourceFileCount,
  OffsetDateTime generatedAt
) {
}

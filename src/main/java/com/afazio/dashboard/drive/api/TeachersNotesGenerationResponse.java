package com.afazio.dashboard.drive.api;

import java.util.List;

public record TeachersNotesGenerationResponse(
  String periodo,
  int requestedCourses,
  int generatedCourses,
  List<TeachersNotesCourseResponse> items
) {
}

package com.afazio.dashboard.calendar.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MeetingUrlExtractor {

  private static final Pattern URL_PATTERN = Pattern.compile(
    "(https?://[^\\s\\\"'<>]+|meet\\.google\\.com/[A-Za-z0-9-]+)",
    Pattern.CASE_INSENSITIVE
  );

  public String extract(String title, String description, String location, String hangoutLink, Object conferenceData) {
    String explicitConferenceLink = firstNonBlank(
      hangoutLink,
      extractConferenceEntryPointUri(conferenceData)
    );
    if (explicitConferenceLink != null) {
      return sanitize(explicitConferenceLink);
    }

    String detected = firstDetectedUrl(location, description, title);
    return detected != null ? sanitize(detected) : null;
  }

  private String extractConferenceEntryPointUri(Object conferenceData) {
    if (!(conferenceData instanceof Map<?, ?> conferenceMap)) {
      return null;
    }

    Object entryPointsObj = conferenceMap.get("entryPoints");
    if (!(entryPointsObj instanceof List<?> entryPoints)) {
      return null;
    }

    for (Object entryPointObj : entryPoints) {
      if (!(entryPointObj instanceof Map<?, ?> entryPoint)) {
        continue;
      }

      String uri = asString(entryPoint.get("uri"));
      if (uri != null && looksLikeMeetingUrl(uri)) {
        return uri;
      }
    }

    return null;
  }

  private String firstDetectedUrl(String... values) {
    for (String value : values) {
      if (value == null || value.isBlank()) {
        continue;
      }

      Matcher matcher = URL_PATTERN.matcher(value);
      while (matcher.find()) {
        String candidate = matcher.group(1);
        if (looksLikeMeetingUrl(candidate)) {
          return candidate;
        }
      }
    }

    return null;
  }

  private boolean looksLikeMeetingUrl(String value) {
    String normalized = value.toLowerCase(Locale.ROOT);
    return normalized.contains("meet.google.com")
      || normalized.contains("teams.microsoft.com")
      || normalized.contains("zoom.us")
      || normalized.contains("webex.com")
      || normalized.contains("whereby.com")
      || normalized.contains("jitsi")
      || normalized.contains("meetup-join");
  }

  private String sanitize(String value) {
    String sanitized = value.trim();
    if (sanitized.startsWith("meet.google.com/")) {
      return "https://" + sanitized;
    }
    return sanitized;
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private String asString(Object value) {
    return value != null ? value.toString() : null;
  }
}

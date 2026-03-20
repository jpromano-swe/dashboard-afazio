package com.afazio.dashboard.calendar.application;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GoogleCalendarEventFetcher {

  private final RestClient restClient;
  private final MeetingUrlExtractor meetingUrlExtractor;

  public GoogleCalendarEventFetcher(RestClient.Builder restClientBuilder, MeetingUrlExtractor meetingUrlExtractor) {
    this.restClient = restClientBuilder
      .baseUrl("https://www.googleapis.com")
      .build();
    this.meetingUrlExtractor = meetingUrlExtractor;
  }

  public List<CalendarClassEvent> fetch(
    OAuth2AuthorizedClient authorizedClient,
    OffsetDateTime from,
    OffsetDateTime to
  ) {
    Object response = restClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/calendar/v3/calendars/primary/events")
        .queryParam("singleEvents", true)
        .queryParam("conferenceDataVersion", 1)
        .queryParam("timeMin", from.withNano(0).toInstant().toString())
        .queryParam("timeMax", to.withNano(0).toInstant().toString())
        .queryParam("maxResults", 50)
        .build())
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .body(Object.class);

    if (!(response instanceof Map<?, ?> body)) {
      throw new IllegalStateException("Respuesta inesperada de Google Calendar");
    }

    Object itemsObj = body.get("items");
    if (!(itemsObj instanceof List<?> items)) {
      return List.of();
    }

    List<CalendarClassEvent> result = new ArrayList<>();

    for (Object itemObj : items) {
      if (!(itemObj instanceof Map<?, ?> item)) {
        continue;
      }

      String id = asString(item.get("id"));
      String title = asString(item.get("summary"));
      String description = asString(item.get("description"));
      String location = asString(item.get("location"));
      String hangoutLink = asString(item.get("hangoutLink"));
      String meetingUrl = meetingUrlExtractor.extract(
        title,
        description,
        location,
        hangoutLink,
        item.get("conferenceData")
      );

      OffsetDateTime startAt = extractDateTime(item.get("start"));
      OffsetDateTime endAt = extractDateTime(item.get("end"));

      if (id == null || title == null || startAt == null || endAt == null) {
        continue;
      }

      if (shouldSkipEvent(title)) {
        continue;
      }

      result.add(new CalendarClassEvent(
        id,
        title,
        description,
        meetingUrl,
        startAt,
        endAt,
        "Accenture"
      ));
    }

    return result;
  }

  private OffsetDateTime extractDateTime(Object node) {
    if (!(node instanceof Map<?, ?> map)) {
      return null;
    }

    String dateTime = asString(map.get("dateTime"));
    if (dateTime == null) {
      return null;
    }

    return OffsetDateTime.parse(dateTime);
  }

  private String asString(Object value) {
    return value != null ? value.toString() : null;
  }

  private boolean shouldSkipEvent(String title) {
    String normalized = title.toLowerCase(Locale.ROOT).trim();

    return normalized.contains("cumple")
      || normalized.contains("contenido")
      || normalized.contains("facturar")
      || normalized.equals("mamá")
      || normalized.equals("juanpi")
      || normalized.equals("blc");
  }
}

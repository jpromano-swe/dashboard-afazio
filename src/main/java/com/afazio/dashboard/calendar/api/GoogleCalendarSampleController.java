package com.afazio.dashboard.calendar.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class GoogleCalendarSampleController {

  private final RestClient restClient;

  public GoogleCalendarSampleController(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder
      .baseUrl("https://www.googleapis.com")
      .build();
  }

  @GetMapping("/google/calendar/sample")
  public List<Map<String, Object>> sample(
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
    @RequestParam OffsetDateTime from,
    @RequestParam OffsetDateTime to
  ) {
    Object response = restClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/calendar/v3/calendars/primary/events")
        .queryParam("timeMin", from.withNano(0).toInstant().toString())
        .queryParam("timeMax", to.withNano(0).toInstant().toString())
        .queryParam("singleEvents", true)
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

    List<Map<String, Object>> result = new ArrayList<>();

    for (Object itemObj : items) {
      if (!(itemObj instanceof Map<?, ?> item)) {
        continue;
      }

      result.add(Map.of(
        "id", value(item.get("id")),
        "summary", value(item.get("summary")),
        "description", value(item.get("description")),
        "start", item.get("start"),
        "end", item.get("end"),
        "recurrence", item.get("recurrence") != null ? item.get("recurrence") : List.of()
      ));
    }

    return result;
  }

  private Object value(Object value) {
    return value != null ? value : "";
  }
}

package com.afazio.dashboard.calendar.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class GoogleCalendarDebugController {

  private final RestClient restClient;

  public GoogleCalendarDebugController(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder
      .baseUrl("https://www.googleapis.com")
      .build();
  }

  @GetMapping("/google/calendar/events")
  public Object listarEventos(
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
    @RequestParam(defaultValue = "10") int maxResults
  ) {
    return restClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/calendar/v3/calendars/primary/events")
        .queryParam("maxResults", maxResults)
        .queryParam("singleEvents", true)
        .queryParam("orderBy", "startTime")
        .build())
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .body(Object.class);
  }
}

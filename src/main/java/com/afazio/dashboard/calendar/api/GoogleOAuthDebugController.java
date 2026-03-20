package com.afazio.dashboard.calendar.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@RestController
public class GoogleOAuthDebugController {

  @GetMapping("/google/debug")
  public Map<String, Object> debug(
    @AuthenticationPrincipal OAuth2User user,
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
  ) {
    assert authorizedClient.getAccessToken().getExpiresAt() != null;
    return Map.of(
      "name", Objects.requireNonNull(user.getAttribute("name")),
      "email", Objects.requireNonNull(user.getAttribute("email")),
      "clientRegistration", authorizedClient.getClientRegistration().getRegistrationId(),
      "accessTokenExpiresAt", authorizedClient.getAccessToken().getExpiresAt(),
      "hasRefreshToken", authorizedClient.getRefreshToken() != null,
      "now", Instant.now()
    );
  }
}

package com.afazio.dashboard.drive.api;

import com.afazio.dashboard.drive.application.TeachersNotesService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/google/drive/teachers-notes")
public class TeachersNotesController {

  private final TeachersNotesService teachersNotesService;

  public TeachersNotesController(TeachersNotesService teachersNotesService) {
    this.teachersNotesService = teachersNotesService;
  }

  @GetMapping
  public List<TeachersNotesCourseResponse> listar(
    @ModelAttribute @Valid TeachersNotesGenerateRequest request
  ) {
    return teachersNotesService.listar(request);
  }

  @PostMapping("/generate")
  public TeachersNotesGenerationResponse generar(
    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
    @RequestBody @Valid TeachersNotesGenerateRequest request
  ) {
    return teachersNotesService.generar(authorizedClient, request);
  }
}

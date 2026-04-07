package com.afazio.dashboard.drive.application;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class GoogleDriveApiClient {

  private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
  private static final String GOOGLE_DOC_MIME_TYPE = "application/vnd.google-apps.document";
  private static final String GOOGLE_DOC_EXPORT_TEXT = "text/plain";

  private final RestClient driveClient;
  private final RestClient docsClient;

  public GoogleDriveApiClient(RestClient.Builder restClientBuilder) {
    this.driveClient = restClientBuilder
      .baseUrl("https://www.googleapis.com/drive/v3")
      .build();
    this.docsClient = restClientBuilder
      .baseUrl("https://docs.googleapis.com/v1")
      .build();
  }

  public List<DriveItem> listChildren(OAuth2AuthorizedClient client, String parentId) {
    return listChildren(client, parentId, null);
  }

  public List<DriveItem> listChildren(OAuth2AuthorizedClient client, String parentId, String mimeType) {
    List<DriveItem> result = new ArrayList<>();
    String nextPageToken = null;

    do {
      String pageToken = nextPageToken;
      Object response = driveClient.get()
        .uri(uriBuilder -> {
          uriBuilder
            .path("/files")
            .queryParam("spaces", "drive")
            .queryParam("pageSize", 1000)
            .queryParam("fields", "nextPageToken,files(id,name,mimeType,webViewLink,modifiedTime)")
            .queryParam("q", buildParentQuery(parentId, mimeType));
          if (pageToken != null) {
            uriBuilder.queryParam("pageToken", pageToken);
          }
          return uriBuilder.build();
        })
        .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(Object.class);

      if (!(response instanceof Map<?, ?> body)) {
        throw new IllegalStateException("Respuesta inesperada al listar archivos de Drive");
      }

      Object filesObj = body.get("files");
      if (filesObj instanceof List<?> files) {
        for (Object fileObj : files) {
          if (fileObj instanceof Map<?, ?> file) {
            result.add(toDriveItem(file));
          }
        }
      }

      nextPageToken = asString(body.get("nextPageToken"));
    } while (nextPageToken != null && !nextPageToken.isBlank());

    return result;
  }

  public DriveItem createFolder(OAuth2AuthorizedClient client, String parentId, String folderName) {
    Object response = driveClient.post()
      .uri(uriBuilder -> uriBuilder
        .path("/files")
        .queryParam("fields", "id,name,mimeType,webViewLink")
        .build())
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .body(Map.of(
        "name", folderName,
        "mimeType", FOLDER_MIME_TYPE,
        "parents", List.of(parentId)
      ))
      .retrieve()
      .body(Object.class);

    if (!(response instanceof Map<?, ?> body)) {
      throw new IllegalStateException("No se pudo crear la carpeta de Drive");
    }

    return toDriveItem(body);
  }

  public DriveItem createGoogleDocument(OAuth2AuthorizedClient client, String parentId, String documentTitle) {
    Object response = driveClient.post()
      .uri(uriBuilder -> uriBuilder
        .path("/files")
        .queryParam("fields", "id,name,mimeType,webViewLink")
        .build())
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .body(Map.of(
        "name", documentTitle,
        "mimeType", GOOGLE_DOC_MIME_TYPE,
        "parents", List.of(parentId)
      ))
      .retrieve()
      .body(Object.class);

    if (!(response instanceof Map<?, ?> body)) {
      throw new IllegalStateException("No se pudo crear el documento de Google Docs");
    }

    return toDriveItem(body);
  }

  public void writeDocument(OAuth2AuthorizedClient client, String documentId, String content) {
    batchUpdateDocument(client, documentId, List.of(
      Map.of(
        "insertText",
        Map.of(
          "location", Map.of("index", 1),
          "text", content
        )
      )
    ));
  }

  public void batchUpdateDocument(OAuth2AuthorizedClient client, String documentId, List<Map<String, Object>> requests) {
    docsClient.post()
      .uri("/documents/{documentId}:batchUpdate", documentId)
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .body(Map.of("requests", requests))
      .retrieve()
      .toBodilessEntity();
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getGoogleDoc(OAuth2AuthorizedClient client, String documentId) {
    Map<String, Object> response = docsClient.get()
      .uri("/documents/{documentId}", documentId)
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .body(Map.class);

    if (response == null) {
      throw new IllegalStateException("No se pudo leer el documento de Google Docs");
    }

    return response;
  }

  public String exportGoogleDocAsText(OAuth2AuthorizedClient client, String fileId) {
    byte[] bytes = driveClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/files/{fileId}/export")
        .queryParam("mimeType", GOOGLE_DOC_EXPORT_TEXT)
        .build(fileId))
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .accept(MediaType.TEXT_PLAIN)
      .retrieve()
      .body(byte[].class);

    if (bytes == null) {
      throw new IllegalStateException("No se pudo exportar el documento de Google Docs");
    }

    return new String(bytes, StandardCharsets.UTF_8);
  }

  public String downloadPlainText(OAuth2AuthorizedClient client, String fileId) {
    byte[] bytes = driveClient.get()
      .uri("/files/{fileId}?alt=media", fileId)
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .retrieve()
      .body(byte[].class);

    if (bytes == null) {
      throw new IllegalStateException("No se pudo descargar el archivo de texto");
    }

    return new String(bytes, StandardCharsets.UTF_8);
  }

  public String downloadDocxText(OAuth2AuthorizedClient client, String fileId) {
    byte[] bytes = driveClient.get()
      .uri("/files/{fileId}?alt=media", fileId)
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .retrieve()
      .body(byte[].class);

    if (bytes == null) {
      throw new IllegalStateException("No se pudo descargar el archivo DOCX");
    }

    try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
         XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
      return extractor.getText();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo extraer texto del archivo DOCX", e);
    }
  }

  public byte[] downloadDocxBytes(OAuth2AuthorizedClient client, String fileId) {
    byte[] bytes = driveClient.get()
      .uri("/files/{fileId}?alt=media", fileId)
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .retrieve()
      .body(byte[].class);

    if (bytes == null) {
      throw new IllegalStateException("No se pudo descargar el archivo DOCX");
    }

    return bytes;
  }

  public void deleteFile(OAuth2AuthorizedClient client, String fileId) {
    driveClient.delete()
      .uri("/files/{fileId}", fileId)
      .header(HttpHeaders.AUTHORIZATION, bearerToken(client))
      .retrieve()
      .toBodilessEntity();
  }

  public DriveItem findFolderByName(OAuth2AuthorizedClient client, String parentId, String folderName) {
    return listChildren(client, parentId, FOLDER_MIME_TYPE)
      .stream()
      .filter(item -> folderName.equalsIgnoreCase(item.name()))
      .findFirst()
      .orElse(null);
  }

  public DriveItem findFileByName(OAuth2AuthorizedClient client, String parentId, String fileName) {
    return listChildren(client, parentId)
      .stream()
      .filter(item -> fileName.equalsIgnoreCase(item.name()))
      .findFirst()
      .orElse(null);
  }

  private DriveItem toDriveItem(Map<?, ?> body) {
    return new DriveItem(
      asString(body.get("id")),
      asString(body.get("name")),
      asString(body.get("mimeType")),
      asString(body.get("webViewLink")),
      asString(body.get("modifiedTime"))
    );
  }

  private String buildParentQuery(String parentId, String mimeType) {
    StringBuilder query = new StringBuilder();
    query.append("'").append(escapeDriveQueryValue(parentId)).append("' in parents and trashed = false");
    if (mimeType != null && !mimeType.isBlank()) {
      query.append(" and mimeType = '").append(escapeDriveQueryValue(mimeType)).append("'");
    }
    return query.toString();
  }

  private String bearerToken(OAuth2AuthorizedClient client) {
    return "Bearer " + client.getAccessToken().getTokenValue();
  }

  private String asString(Object value) {
    return value != null ? value.toString() : null;
  }

  private String escapeDriveQueryValue(String value) {
    return value.replace("'", "\\'");
  }

  public record DriveItem(
    String id,
    String name,
    String mimeType,
    String webViewLink,
    String modifiedTime
  ) {
  }
}

package com.afazio.dashboard.drive.application;

import com.afazio.dashboard.core.domain.Clase;
import com.afazio.dashboard.core.domain.Consultora;
import com.afazio.dashboard.core.domain.Curso;
import com.afazio.dashboard.core.infrastructure.ClaseRepository;
import com.afazio.dashboard.core.infrastructure.CursoRepository;
import com.afazio.dashboard.drive.api.TeachersNotesCourseResponse;
import com.afazio.dashboard.drive.api.TeachersNotesGenerationResponse;
import com.afazio.dashboard.drive.api.TeachersNotesGenerateRequest;
import com.afazio.dashboard.drive.domain.TeacherNoteDocument;
import com.afazio.dashboard.drive.infrastructure.TeacherNoteDocumentRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TeachersNotesService {

  private static final String DONE_FOLDER_NAME = "DONE";
  private static final Locale SPANISH_AR = new Locale("es", "AR");

  private final GoogleDriveProperties googleDriveProperties;
  private final GoogleDriveApiClient googleDriveApiClient;
  private final CursoRepository cursoRepository;
  private final TeacherNoteDocumentRepository teacherNoteDocumentRepository;

  public TeachersNotesService(
    GoogleDriveProperties googleDriveProperties,
    GoogleDriveApiClient googleDriveApiClient,
    CursoRepository cursoRepository,
    TeacherNoteDocumentRepository teacherNoteDocumentRepository
  ) {
    this.googleDriveProperties = googleDriveProperties;
    this.googleDriveApiClient = googleDriveApiClient;
    this.cursoRepository = cursoRepository;
    this.teacherNoteDocumentRepository = teacherNoteDocumentRepository;
  }

  @Transactional(readOnly = true)
  public List<TeachersNotesCourseResponse> listar(TeachersNotesGenerateRequest request) {
    String periodo = request.periodo().toString();

    if (request.cursoIds() == null || request.cursoIds().isEmpty()) {
      return teacherNoteDocumentRepository.findByPeriodoOrderByCursoEmpresaAscCursoGrupoAsc(periodo)
        .stream()
        .map(document -> toResponse(document, "GENERATED", "Documento disponible"))
        .toList();
    }

    List<Curso> cursos = resolveCursos(request.cursoIds());
    return teacherNoteDocumentRepository.findByPeriodoAndCursoIdInOrderByCursoEmpresaAscCursoGrupoAsc(
        periodo,
        cursos.stream().map(Curso::getId).toList()
      )
      .stream()
      .map(document -> toResponse(document, "GENERATED", "Documento disponible"))
      .toList();
  }

  @Transactional
  public TeachersNotesGenerationResponse generar(OAuth2AuthorizedClient authorizedClient, TeachersNotesGenerateRequest request) {
    if (!StringUtils.hasText(googleDriveProperties.rootFolderId())) {
      throw new IllegalStateException("No está configurado app.google.drive.root-folder-id");
    }

    List<Curso> cursos = resolveCursos(request.cursoIds());
    String periodo = request.periodo().toString();
    List<TeachersNotesCourseResponse> results = new ArrayList<>();

    for (Curso curso : cursos) {
      results.add(generarParaCurso(authorizedClient, curso, request.periodo(), periodo));
    }

    int generated = (int) results.stream().filter(item -> "GENERATED".equals(item.status())).count();

    return new TeachersNotesGenerationResponse(
      periodo,
      cursos.size(),
      generated,
      results
    );
  }

  private TeachersNotesCourseResponse generarParaCurso(
    OAuth2AuthorizedClient authorizedClient,
    Curso curso,
    YearMonth periodo,
    String periodoKey
  ) {
    DriveContext driveContext = resolveDriveContext(authorizedClient, curso, periodo);
    List<GoogleDriveApiClient.DriveItem> sourceFiles = listarArchivosSoportados(authorizedClient, driveContext.monthFolder.id());

    if (sourceFiles.isEmpty()) {
      return new TeachersNotesCourseResponse(
        curso.getId(),
        courseDisplayName(curso),
        periodoKey,
        "NO_FILES",
        "No hay archivos soportados en DONE/" + driveContext.monthFolder.name(),
        null,
        null,
        driveContext.folderPath(),
        null,
        0,
        OffsetDateTime.now()
      );
    }

    String documentTitle = documentTitle(periodo);
    RichDocument mergedDocument = buildDocumentContent(curso, periodo, sourceFiles, authorizedClient);

    TeacherNoteDocument existing = teacherNoteDocumentRepository.findByCursoAndPeriodo(curso, periodoKey).orElse(null);
    GoogleDriveApiClient.DriveItem created = googleDriveApiClient.createGoogleDocument(
      authorizedClient,
      driveContext.monthFolder.id(),
      documentTitle
    );
    googleDriveApiClient.batchUpdateDocument(authorizedClient, created.id(), mergedDocument.requests());

    TeacherNoteDocument document = existing != null ? existing : new TeacherNoteDocument();
    document.setCurso(curso);
    document.setPeriodo(periodoKey);
    document.setDriveFileId(created.id());
    document.setDriveUrl(created.webViewLink() != null ? created.webViewLink() : buildDocsUrl(created.id()));
    document.setDocumentTitle(documentTitle);
    document.setFolderPath(driveContext.folderPath());
    document.setSourceFileCount(sourceFiles.size());

    TeacherNoteDocument saved = teacherNoteDocumentRepository.save(document);

    if (existing != null && StringUtils.hasText(existing.getDriveFileId()) && !existing.getDriveFileId().equals(created.id())) {
      try {
        googleDriveApiClient.deleteFile(authorizedClient, existing.getDriveFileId());
      } catch (Exception ignored) {
        // best effort cleanup
      }
    }

    return toResponse(saved, "GENERATED", "Documento generado correctamente");
  }

  private DriveContext resolveDriveContext(OAuth2AuthorizedClient authorizedClient, Curso curso, YearMonth periodo) {
    GoogleDriveApiClient.DriveItem courseFolder = resolveCourseFolder(authorizedClient, curso);
    GoogleDriveApiClient.DriveItem doneFolder = resolveOrCreateFolder(authorizedClient, courseFolder.id(), DONE_FOLDER_NAME);
    GoogleDriveApiClient.DriveItem monthFolder = resolveMonthFolder(authorizedClient, doneFolder.id(), periodo);

    return new DriveContext(courseFolder, doneFolder, monthFolder, courseFolder.name() + "/" + doneFolder.name() + "/" + monthFolder.name());
  }

  private GoogleDriveApiClient.DriveItem resolveCourseFolder(OAuth2AuthorizedClient authorizedClient, Curso curso) {
    String rootFolderId = googleDriveProperties.rootFolderId();

    List<String> specificCandidates = courseFolderSpecificCandidates(curso);

    for (String candidate : specificCandidates) {
      GoogleDriveApiClient.DriveItem folder = googleDriveApiClient.findFolderByName(authorizedClient, rootFolderId, candidate);
      if (folder != null) {
        return folder;
      }
    }

    GoogleDriveApiClient.DriveItem nested = findFolderRecursively(authorizedClient, rootFolderId, specificCandidates, 4);
    if (nested != null) {
      return nested;
    }

    if (StringUtils.hasText(curso.getEmpresa())) {
      GoogleDriveApiClient.DriveItem companyFolder = googleDriveApiClient.findFolderByName(authorizedClient, rootFolderId, curso.getEmpresa());
      if (companyFolder != null) {
        return companyFolder;
      }
    }

    throw new IllegalArgumentException(
      "No se encontró la carpeta de Drive para el curso " + courseDisplayName(curso)
    );
  }

  private GoogleDriveApiClient.DriveItem resolveOrCreateFolder(
    OAuth2AuthorizedClient authorizedClient,
    String parentId,
    String folderName
  ) {
    GoogleDriveApiClient.DriveItem folder = googleDriveApiClient.findFolderByName(authorizedClient, parentId, folderName);
    if (folder != null) {
      return folder;
    }

    return googleDriveApiClient.createFolder(authorizedClient, parentId, folderName);
  }

  private GoogleDriveApiClient.DriveItem resolveMonthFolder(
    OAuth2AuthorizedClient authorizedClient,
    String parentId,
    YearMonth periodo
  ) {
    for (String candidate : monthFolderCandidates(periodo)) {
      GoogleDriveApiClient.DriveItem folder = googleDriveApiClient.findFolderByName(authorizedClient, parentId, candidate);
      if (folder != null) {
        return folder;
      }
    }

    return googleDriveApiClient.createFolder(authorizedClient, parentId, monthFolderName(periodo));
  }

  private List<GoogleDriveApiClient.DriveItem> listarArchivosSoportados(
    OAuth2AuthorizedClient authorizedClient,
    String parentFolderId
  ) {
    return googleDriveApiClient.listChildren(authorizedClient, parentFolderId)
      .stream()
      .filter(item -> item.mimeType() == null || !item.mimeType().equals("application/vnd.google-apps.folder"))
      .filter(item -> item.name() == null || !item.name().startsWith("Teachers_notes_"))
      .sorted(Comparator.comparing(GoogleDriveApiClient.DriveItem::name, String.CASE_INSENSITIVE_ORDER))
      .toList();
  }

  private RichDocument buildDocumentContent(
    Curso curso,
    YearMonth periodo,
    List<GoogleDriveApiClient.DriveItem> files,
    OAuth2AuthorizedClient authorizedClient
  ) {
    RichDocumentBuilder builder = new RichDocumentBuilder();
    builder.appendParagraph("Teachers notes - " + monthLabel(periodo), ParagraphKind.HEADING_1, false, null, List.of(), true);
    builder.appendParagraph("Curso: " + courseDisplayName(curso), ParagraphKind.NORMAL, false, null, List.of(), true);
    builder.appendParagraph("Periodo: " + periodo, ParagraphKind.NORMAL, false, null, List.of(), false);
    builder.appendBlankLine();

    for (GoogleDriveApiClient.DriveItem file : files) {
      builder.appendParagraph("===== " + file.name() + " =====", ParagraphKind.NORMAL, false, null, List.of(), true);
      FileContent content = extractFileContent(authorizedClient, file);
      if (content.paragraphs().isEmpty()) {
        builder.appendParagraph("[Sin contenido]", ParagraphKind.NORMAL, false, null, List.of(), false);
      } else {
        for (RichParagraph paragraph : content.paragraphs()) {
          builder.appendParagraph(
            paragraph.text(),
            paragraph.kind(),
            paragraph.bullet(),
            paragraph.namedStyleType(),
            paragraph.spans(),
            paragraph.separatorBold()
          );
        }
      }
      builder.appendBlankLine();
    }

    return builder.build();
  }

  private FileContent extractFileContent(OAuth2AuthorizedClient authorizedClient, GoogleDriveApiClient.DriveItem file) {
    String mimeType = file.mimeType();
    if (mimeType == null) {
      return FileContent.empty();
    }

    if ("application/vnd.google-apps.document".equals(mimeType)) {
      return parseGoogleDoc(googleDriveApiClient.getGoogleDoc(authorizedClient, file.id()));
    }

    if ("text/plain".equals(mimeType)) {
      return parsePlainText(googleDriveApiClient.downloadPlainText(authorizedClient, file.id()));
    }

    if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)) {
      return parseDocx(googleDriveApiClient.downloadDocxBytes(authorizedClient, file.id()));
    }

    return FileContent.ofParagraphs(List.of(
      new RichParagraph(
        "[Archivo omitido por formato no soportado: " + mimeType + "]",
        ParagraphKind.NORMAL,
        false,
        null,
        List.of(),
        false
      )
    ));
  }

  private List<Curso> resolveCursos(List<Long> cursoIds) {
    if (cursoIds == null || cursoIds.isEmpty()) {
      throw new IllegalArgumentException("Debe enviar al menos un cursoId");
    }

    Map<Long, Curso> cursosPorId = new LinkedHashMap<>();
    cursoRepository.findAllById(cursoIds).forEach(curso -> cursosPorId.put(curso.getId(), curso));

    if (cursosPorId.size() != cursoIds.size()) {
      throw new IllegalArgumentException("Uno o más cursos no existen");
    }

    return cursoIds.stream()
      .map(cursosPorId::get)
      .map(this::validateCurso)
      .toList();
  }

  private Curso validateCurso(Curso curso) {
    if (!curso.isActiva()) {
      throw new IllegalArgumentException("El curso " + courseDisplayName(curso) + " está inactivo");
    }
    return curso;
  }

  private TeachersNotesCourseResponse toResponse(TeacherNoteDocument document, String status, String message) {
    return new TeachersNotesCourseResponse(
      document.getCurso().getId(),
      courseDisplayName(document.getCurso()),
      document.getPeriodo(),
      status,
      message,
      document.getDriveFileId(),
      document.getDriveUrl(),
      document.getFolderPath(),
      document.getDocumentTitle(),
      document.getSourceFileCount(),
      document.getUpdatedAt()
    );
  }

  private String courseDisplayName(Curso curso) {
    StringBuilder builder = new StringBuilder();
    if (StringUtils.hasText(curso.getEmpresa())) {
      builder.append(curso.getEmpresa());
    }
    if (StringUtils.hasText(curso.getGrupo())) {
      if (builder.length() > 0) {
        builder.append(" - ");
      }
      builder.append(curso.getGrupo());
    }
    return builder.length() > 0 ? builder.toString() : "Curso " + curso.getId();
  }

  private String monthLabel(YearMonth periodo) {
    String label = periodo.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", SPANISH_AR));
    return Character.toUpperCase(label.charAt(0)) + label.substring(1);
  }

  private String documentTitle(YearMonth periodo) {
    return "Teachers_notes_" + monthLabel(periodo).replace(' ', '_');
  }

  private String monthFolderName(YearMonth periodo) {
    return monthLabel(periodo);
  }

  private List<String> monthFolderCandidates(YearMonth periodo) {
    String monthName = periodo.atDay(1).format(DateTimeFormatter.ofPattern("MMMM", SPANISH_AR));
    String monthTitle = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);

    List<String> candidates = new ArrayList<>();
    candidates.add(monthName.toUpperCase(SPANISH_AR));
    candidates.add(monthTitle);
    candidates.add(monthTitle + " " + periodo.getYear());
    candidates.add(periodo.toString());
    return candidates;
  }

  private GoogleDriveApiClient.DriveItem findFolderRecursively(
    OAuth2AuthorizedClient authorizedClient,
    String parentId,
    List<String> candidates,
    int maxDepth
  ) {
    if (maxDepth < 0) {
      return null;
    }

    for (GoogleDriveApiClient.DriveItem child : googleDriveApiClient.listChildren(authorizedClient, parentId)) {
      if (!"application/vnd.google-apps.folder".equals(child.mimeType())) {
        continue;
      }

      for (String candidate : candidates) {
        if (candidate.equalsIgnoreCase(child.name())) {
          return child;
        }
      }

      GoogleDriveApiClient.DriveItem nested = findFolderRecursively(authorizedClient, child.id(), candidates, maxDepth - 1);
      if (nested != null) {
        return nested;
      }
    }

    return null;
  }

  private List<String> courseFolderSpecificCandidates(Curso curso) {
    List<String> candidates = new ArrayList<>();
    String empresa = curso.getEmpresa();
    String grupo = curso.getGrupo();

    if (StringUtils.hasText(empresa) && StringUtils.hasText(grupo)) {
      candidates.add(empresa + " - " + grupo);
      candidates.add(empresa + ": " + grupo);
      candidates.add(empresa + " " + grupo);
    }

    if (StringUtils.hasText(grupo)) {
      candidates.add(grupo);
    }

    return candidates;
  }

  private String buildDocsUrl(String documentId) {
    return "https://docs.google.com/document/d/" + documentId + "/edit";
  }

  private record DriveContext(
    GoogleDriveApiClient.DriveItem courseFolder,
    GoogleDriveApiClient.DriveItem doneFolder,
    GoogleDriveApiClient.DriveItem monthFolder,
    String folderPath
  ) {
  }

  private FileContent parseGoogleDoc(Map<String, Object> document) {
    List<RichParagraph> paragraphs = new ArrayList<>();
    Map<String, Object> body = asMap(document.get("body"));
    List<?> content = asList(body != null ? body.get("content") : null);

    for (Object elementObj : content) {
      Map<String, Object> element = asMap(elementObj);
      Map<String, Object> paragraphNode = asMap(element != null ? element.get("paragraph") : null);
      if (paragraphNode == null) {
        continue;
      }

      paragraphs.add(parseGoogleParagraph(paragraphNode));
    }

    return new FileContent(paragraphs);
  }

  private RichParagraph parseGoogleParagraph(Map<String, Object> paragraphNode) {
    List<TextSpan> spans = new ArrayList<>();
    StringBuilder text = new StringBuilder();
    int cursor = 0;

    List<?> elements = asList(paragraphNode.get("elements"));
    for (Object elementObj : elements) {
      Map<String, Object> element = asMap(elementObj);
      Map<String, Object> textRun = asMap(element != null ? element.get("textRun") : null);
      if (textRun == null) {
        continue;
      }

      String content = asString(textRun.get("content"));
      if (content == null) {
        content = "";
      }
      if (content.endsWith("\n")) {
        content = content.substring(0, content.length() - 1);
      }
      if (content.isEmpty()) {
        continue;
      }

      int start = cursor;
      text.append(content);
      cursor += content.length();
      Map<String, Object> style = asMap(textRun.get("textStyle"));
      spans.add(new TextSpan(
        start,
        cursor,
        asBoolean(style != null ? style.get("bold") : null),
        asBoolean(style != null ? style.get("italic") : null),
        asBoolean(style != null ? style.get("underline") : null)
      ));
    }

    Map<String, Object> paragraphStyle = asMap(paragraphNode.get("paragraphStyle"));
    String namedStyleType = paragraphStyle != null ? asString(paragraphStyle.get("namedStyleType")) : null;
    boolean bullet = paragraphNode.get("bullet") != null;
    ParagraphKind kind = paragraphKindFromNamedStyle(namedStyleType);

    return new RichParagraph(text.toString(), kind, bullet, namedStyleType, spans, false);
  }

  private FileContent parsePlainText(String content) {
    if (!StringUtils.hasText(content)) {
      return FileContent.empty();
    }

    List<RichParagraph> paragraphs = new ArrayList<>();
    for (String line : content.split("\\R", -1)) {
      paragraphs.add(new RichParagraph(line, ParagraphKind.NORMAL, false, null, List.of(), false));
    }
    return new FileContent(paragraphs);
  }

  private FileContent parseDocx(byte[] bytes) {
    List<RichParagraph> paragraphs = new ArrayList<>();

    try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
      for (XWPFParagraph paragraph : document.getParagraphs()) {
        StringBuilder text = new StringBuilder();
        List<TextSpan> spans = new ArrayList<>();
        int cursor = 0;

        for (XWPFRun run : paragraph.getRuns()) {
          String runText = run.getText(0);
          if (runText == null) {
            runText = run.toString();
          }
          if (!StringUtils.hasText(runText)) {
            continue;
          }

          int start = cursor;
          text.append(runText);
          cursor += runText.length();
          spans.add(new TextSpan(
            start,
            cursor,
            run.isBold(),
            run.isItalic(),
            run.getUnderline() != null && run.getUnderline() != UnderlinePatterns.NONE
          ));
        }

        String paragraphText = text.toString();
        ParagraphKind kind = paragraphKindFromStyleId(paragraph.getStyle());
        boolean bullet = paragraph.getNumID() != null;

        paragraphs.add(new RichParagraph(paragraphText, kind, bullet, null, spans, false));
      }
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo extraer el formato del archivo DOCX", e);
    }

    return new FileContent(paragraphs);
  }

  private ParagraphKind paragraphKindFromNamedStyle(String namedStyleType) {
    if (namedStyleType == null) {
      return ParagraphKind.NORMAL;
    }

    return switch (namedStyleType) {
      case "HEADING_1" -> ParagraphKind.HEADING_1;
      case "HEADING_2" -> ParagraphKind.HEADING_2;
      case "HEADING_3" -> ParagraphKind.HEADING_3;
      default -> ParagraphKind.NORMAL;
    };
  }

  private ParagraphKind paragraphKindFromStyleId(String styleId) {
    if (styleId == null) {
      return ParagraphKind.NORMAL;
    }

    String normalized = styleId.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
    return switch (normalized) {
      case "HEADING1", "HEADING_1" -> ParagraphKind.HEADING_1;
      case "HEADING2", "HEADING_2" -> ParagraphKind.HEADING_2;
      case "HEADING3", "HEADING_3" -> ParagraphKind.HEADING_3;
      default -> ParagraphKind.NORMAL;
    };
  }

  private record FileContent(List<RichParagraph> paragraphs) {
    private static FileContent empty() {
      return new FileContent(List.of());
    }

    private static FileContent ofParagraphs(List<RichParagraph> paragraphs) {
      return new FileContent(paragraphs);
    }
  }

  private enum ParagraphKind {
    NORMAL,
    HEADING_1,
    HEADING_2,
    HEADING_3
  }

  private record TextSpan(int start, int end, boolean bold, boolean italic, boolean underline) {
  }

  private record RichParagraph(
    String text,
    ParagraphKind kind,
    boolean bullet,
    String namedStyleType,
    List<TextSpan> spans,
    boolean separatorBold
  ) {
  }

  private record RichDocument(List<RichParagraph> paragraphs) {
    private List<Map<String, Object>> requests() {
      List<Map<String, Object>> requests = new ArrayList<>();
      int cursor = 1;

      for (RichParagraph paragraph : paragraphs) {
        int startIndex = cursor;
        String text = paragraph.text() == null ? "" : paragraph.text();
        requests.add(insertTextRequest(cursor, text + "\n"));

        if (paragraph.kind() != ParagraphKind.NORMAL) {
          String namedStyleType = switch (paragraph.kind()) {
            case HEADING_1 -> "HEADING_1";
            case HEADING_2 -> "HEADING_2";
            case HEADING_3 -> "HEADING_3";
            default -> null;
          };
          if (namedStyleType != null && !text.isBlank()) {
            requests.add(updateParagraphStyleRequest(startIndex, cursor + text.length() + 1, namedStyleType));
          }
        }

        if (paragraph.bullet()) {
          requests.add(createParagraphBulletsRequest(startIndex, cursor + text.length() + 1));
        }

        for (TextSpan span : paragraph.spans()) {
          if (span.start() == span.end()) {
            continue;
          }
          Map<String, Object> textStyle = new LinkedHashMap<>();
          List<String> fields = new ArrayList<>();
          if (span.bold()) {
            textStyle.put("bold", true);
            fields.add("bold");
          }
          if (span.italic()) {
            textStyle.put("italic", true);
            fields.add("italic");
          }
          if (span.underline()) {
            textStyle.put("underline", true);
            fields.add("underline");
          }
          if (!textStyle.isEmpty()) {
            requests.add(updateTextStyleRequest(
              startIndex + span.start(),
              startIndex + span.end(),
              textStyle,
              String.join(",", fields)
            ));
          }
        }

        cursor += text.length() + 1;
      }

      return requests;
    }
  }

  private static final class RichDocumentBuilder {
    private final List<RichParagraph> paragraphs = new ArrayList<>();

    void appendParagraph(
      String text,
      ParagraphKind kind,
      boolean bullet,
      String namedStyleType,
      List<TextSpan> spans,
      boolean separatorBold
    ) {
      List<TextSpan> effectiveSpans = new ArrayList<>();
      if (spans != null) {
        effectiveSpans.addAll(spans);
      }
      if (separatorBold && text != null && !text.isEmpty()) {
        effectiveSpans.add(new TextSpan(0, text.length(), true, false, false));
      }
      paragraphs.add(new RichParagraph(text, kind, bullet, namedStyleType, effectiveSpans, separatorBold));
    }

    void appendBlankLine() {
      paragraphs.add(new RichParagraph("", ParagraphKind.NORMAL, false, null, List.of(), false));
    }

    RichDocument build() {
      return new RichDocument(paragraphs);
    }
  }

  private static Map<String, Object> insertTextRequest(int index, String text) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("insertText", Map.of(
      "location", Map.of("index", index),
      "text", text
    ));
    return request;
  }

  private static Map<String, Object> updateTextStyleRequest(int startIndex, int endIndex, Map<String, Object> textStyle, String fields) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("updateTextStyle", Map.of(
      "range", Map.of("startIndex", startIndex, "endIndex", endIndex),
      "textStyle", textStyle,
      "fields", fields
    ));
    return request;
  }

  private static Map<String, Object> updateParagraphStyleRequest(int startIndex, int endIndex, String namedStyleType) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("updateParagraphStyle", Map.of(
      "range", Map.of("startIndex", startIndex, "endIndex", endIndex),
      "paragraphStyle", Map.of("namedStyleType", namedStyleType),
      "fields", "namedStyleType"
    ));
    return request;
  }

  private static Map<String, Object> createParagraphBulletsRequest(int startIndex, int endIndex) {
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("createParagraphBullets", Map.of(
      "range", Map.of("startIndex", startIndex, "endIndex", endIndex),
      "bulletPreset", "BULLET_DISC_CIRCLE_SQUARE"
    ));
    return request;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> asMap(Object value) {
    if (value instanceof Map<?, ?> map) {
      return (Map<String, Object>) map;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private List<?> asList(Object value) {
    if (value instanceof List<?> list) {
      return list;
    }
    return List.of();
  }

  private String asString(Object value) {
    return value != null ? value.toString() : null;
  }

  private boolean asBoolean(Object value) {
    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }
    return value != null && Boolean.parseBoolean(value.toString());
  }
}

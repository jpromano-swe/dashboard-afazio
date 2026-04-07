package com.afazio.dashboard.drive.domain;

import com.afazio.dashboard.core.domain.Curso;
import com.afazio.dashboard.shared.domain.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(
  name = "teacher_note_document",
  uniqueConstraints = @UniqueConstraint(name = "uk_teacher_note_document_curso_periodo", columnNames = {"curso_id", "periodo"})
)
public class TeacherNoteDocument extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "curso_id", nullable = false)
  private Curso curso;

  @Column(nullable = false, length = 7)
  private String periodo;

  @Column(name = "drive_file_id", nullable = false, length = 255)
  private String driveFileId;

  @Column(name = "drive_url", nullable = false, length = 1000)
  private String driveUrl;

  @Column(name = "document_title", nullable = false, length = 255)
  private String documentTitle;

  @Column(name = "folder_path", nullable = false, length = 500)
  private String folderPath;

  @Column(name = "source_file_count", nullable = false)
  private int sourceFileCount;

  public Long getId() {
    return id;
  }

  public Curso getCurso() {
    return curso;
  }

  public void setCurso(Curso curso) {
    this.curso = curso;
  }

  public String getPeriodo() {
    return periodo;
  }

  public void setPeriodo(String periodo) {
    this.periodo = periodo;
  }

  public String getDriveFileId() {
    return driveFileId;
  }

  public void setDriveFileId(String driveFileId) {
    this.driveFileId = driveFileId;
  }

  public String getDriveUrl() {
    return driveUrl;
  }

  public void setDriveUrl(String driveUrl) {
    this.driveUrl = driveUrl;
  }

  public String getDocumentTitle() {
    return documentTitle;
  }

  public void setDocumentTitle(String documentTitle) {
    this.documentTitle = documentTitle;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  public int getSourceFileCount() {
    return sourceFileCount;
  }

  public void setSourceFileCount(int sourceFileCount) {
    this.sourceFileCount = sourceFileCount;
  }
}

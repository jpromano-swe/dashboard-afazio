package com.afazio.dashboard.core.domain;

import com.afazio.dashboard.shared.domain.AuditableEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consultora")
public class Consultora extends AuditableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120, unique = true)
  private String nombre;

  @Column(length = 255)
  private String descripcion;

  @Column(nullable=false)
  private boolean activa;

  @Column(name = "requiere_reporte_excel", nullable = false)
  private boolean requiereReporteExcel;

  @Column(name = "google_calendar_id", length = 255)
  private String googleCalendarId;

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public boolean isActiva() {
    return activa;
  }

  public void setActiva(boolean activa) {
    this.activa = activa;
  }

  public String getGoogleCalendarId() {
    return googleCalendarId;
  }

  public void setGoogleCalendarId(String googleCalendarId) {
    this.googleCalendarId = googleCalendarId;
  }

  public boolean isRequiereReporteExcel() {
    return requiereReporteExcel;
  }

  public void setRequiereReporteExcel(boolean requiereReporteExcel) {
    this.requiereReporteExcel = requiereReporteExcel;
  }
}

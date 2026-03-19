package com.afazio.dashboard.core.domain;

import com.afazio.dashboard.shared.domain.AuditableEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "clase")
public class Clase extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "consultora_id", nullable = false)
  private Consultora consultora;

  @Column (nullable = false, length = 180)
  private String titulo;

  @Column (length = 1000)
  private String descripcion;

  @Column(name = "fecha_inicio", nullable = false)
  private OffsetDateTime fechaInicio;

  @Column(name = "fecha_fin", nullable = false)
  private OffsetDateTime fechaFin;

  @Column(name = "duracion_minutos", nullable = false)
  private int duracionMinutos;

  @Column(name = "google_event_id", unique = true, length = 255)
  private String googleEventId;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 30)
  private ClaseEstado estado;

  @Column(name = "sincronizada_en")
  private OffsetDateTime sincronizadaEn;

  public Long getId() {
    return id;
  }

  public Consultora getConsultora() {
    return consultora;
  }

  public void setConsultora(Consultora consultora) {
    this.consultora = consultora;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public OffsetDateTime getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(OffsetDateTime fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public OffsetDateTime getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(OffsetDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }

  public Integer getDuracionMinutos() {
    return duracionMinutos;
  }

  public void setDuracionMinutos(Integer duracionMinutos) {
    this.duracionMinutos = duracionMinutos;
  }

  public String getGoogleEventId() {
    return googleEventId;
  }

  public void setGoogleEventId(String googleEventId) {
    this.googleEventId = googleEventId;
  }

  public ClaseEstado getEstado() {
    return estado;
  }

  public void setEstado(ClaseEstado estado) {
    this.estado = estado;
  }

  public OffsetDateTime getSincronizadaEn() {
    return sincronizadaEn;
  }

  public void setSincronizadaEn(OffsetDateTime sincronizadaEn) {
    this.sincronizadaEn = sincronizadaEn;
  }
}

package com.afazio.dashboard.core.domain;

import com.afazio.dashboard.shared.domain.AuditableEntity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "asistencia")
public class Asistencia extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "clase_id", nullable = false, unique = true)
  private Clase clase;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AsistenciaEstado estado;

  @Column(name = "marcada_en", nullable = false)
  private OffsetDateTime marcadaEn;

  @Column(length = 500)
  private String observacion;

  public Long getId() {
    return id;
  }

  public Clase getClase() {
    return clase;
  }

  public void setClase(Clase clase) {
    this.clase = clase;
  }

  public AsistenciaEstado getEstado() {
    return estado;
  }

  public void setEstado(AsistenciaEstado estado) {
    this.estado = estado;
  }

  public OffsetDateTime getMarcadaEn() {
    return marcadaEn;
  }

  public void setMarcadaEn(OffsetDateTime marcadaEn) {
    this.marcadaEn = marcadaEn;
  }

  public String getObservacion() {
    return observacion;
  }

  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }
}

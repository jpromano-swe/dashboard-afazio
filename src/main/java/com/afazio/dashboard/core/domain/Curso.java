package com.afazio.dashboard.core.domain;

import com.afazio.dashboard.shared.domain.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "curso")
public class Curso extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "consultora_id", nullable = false)
  private Consultora consultora;

  @Column(nullable = false, length = 180)
  private String empresa;

  @Column(length = 180)
  private String grupo;

  @Column(nullable = false)
  private boolean activa;

  public Long getId() {
    return id;
  }

  public Consultora getConsultora() {
    return consultora;
  }

  public void setConsultora(Consultora consultora) {
    this.consultora = consultora;
  }

  public String getEmpresa() {
    return empresa;
  }

  public void setEmpresa(String empresa) {
    this.empresa = empresa;
  }

  public String getGrupo() {
    return grupo;
  }

  public void setGrupo(String grupo) {
    this.grupo = grupo;
  }

  public boolean isActiva() {
    return activa;
  }

  public void setActiva(boolean activa) {
    this.activa = activa;
  }
}

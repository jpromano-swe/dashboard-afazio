package com.afazio.dashboard.core.domain;


import com.afazio.dashboard.shared.domain.AuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tarifa_consultora")
public class TarifaConsultora extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "consultora_id", nullable = false)
  private Consultora consultora;

  @Column(name = "monto_por_hora", nullable = false, precision = 12, scale = 2)
  private BigDecimal montoPorHora;

  @Column(nullable = false, length = 3)
  private String moneda;

  @Column(name = "vigente_desde", nullable = false)
  private LocalDate vigenteDesde;

  @Column(name = "vigente_hasta", nullable = false)
  private LocalDate vigenteHasta;

  @Column(name = "fecha_ultimo_aumento")
  private LocalDate fechaUltimoAumento;

  @Column(length = 255)
  private String observaciones;

  public Long getId() {
    return id;
  }

  public Consultora getConsultora() {
    return consultora;
  }

  public void setConsultora(Consultora consultora) {
    this.consultora = consultora;
  }

  public BigDecimal getMontoPorHora() {
    return montoPorHora;
  }

  public void setMontoPorHora(BigDecimal montoPorHora) {
    this.montoPorHora = montoPorHora;
  }

  public String getMoneda() {
    return moneda;
  }

  public void setMoneda(String moneda) {
    this.moneda = moneda;
  }

  public LocalDate getVigenteDesde() {
    return vigenteDesde;
  }

  public void setVigenteDesde(LocalDate vigenteDesde) {
    this.vigenteDesde = vigenteDesde;
  }

  public LocalDate getVigenteHasta() {
    return vigenteHasta;
  }

  public void setVigenteHasta(LocalDate vigenteHasta) {
    this.vigenteHasta = vigenteHasta;
  }

  public LocalDate getFechaUltimoAumento() {
    return fechaUltimoAumento;
  }

  public void setFechaUltimoAumento(LocalDate fechaUltimoAumento) {
    this.fechaUltimoAumento = fechaUltimoAumento;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }
}

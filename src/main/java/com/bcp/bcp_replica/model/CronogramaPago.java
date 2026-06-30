package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "cronograma_pagos")
public class CronogramaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numeroCuota;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private Double cuotaTotal;

    @Column(nullable = false)
    private Double capital;

    @Column(nullable = false)
    private Double interes;

    @Column(nullable = false)
    private Double saldoRestante;

    // PENDIENTE, PAGADO, VENCIDO
    @Column(nullable = false)
    private String estado = "PENDIENTE";

    @ManyToOne
    @JoinColumn(name = "credito_id", nullable = false)
    private Credito credito;

    // ── Getters y Setters ──────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(Integer n) { this.numeroCuota = n; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }

    public Double getCuotaTotal() { return cuotaTotal; }
    public void setCuotaTotal(Double c) { this.cuotaTotal = c; }

    public Double getCapital() { return capital; }
    public void setCapital(Double c) { this.capital = c; }

    public Double getInteres() { return interes; }
    public void setInteres(Double i) { this.interes = i; }

    public Double getSaldoRestante() { return saldoRestante; }
    public void setSaldoRestante(Double s) { this.saldoRestante = s; }

    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }

    public Credito getCredito() { return credito; }
    public void setCredito(Credito c) { this.credito = c; }
}
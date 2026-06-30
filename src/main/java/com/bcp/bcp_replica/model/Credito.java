package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "creditos")
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroCredito;

    // CONSUMO, HIPOTECARIO, MYPE, CORPORATIVO, PERSONAL
    @Column(nullable = false)
    private String tipoCredito;

    @Column(nullable = false)
    private Double montoAprobado;

    @Column(nullable = false)
    private Double saldoCapital;  // saldo pendiente

    @Column(nullable = false)
    private Integer plazoMeses;

    @Column(nullable = false)
    private Double tasaInteresAnual; // TEA %

    @Column(nullable = false)
    private Double cuotaMensual;

    // VIGENTE, ATRASADO, REFINANCIADO, CANCELADO
    @Column(nullable = false)
    private String estado = "VIGENTE";

    // Días de atraso (para mora)
    private Integer diasAtraso = 0;

    // Cuotas pagadas y pendientes
    private Integer cuotasPagadas = 0;
    private Integer cuotasPendientes;

    private LocalDate fechaDesembolso;
    private LocalDate fechaVencimiento;
    private LocalDate fechaProximaCuota;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToOne
    @JoinColumn(name = "solicitud_id")
    private SolicitudCredito solicitud;

    // ── Getters y Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCredito() { return numeroCredito; }
    public void setNumeroCredito(String n) { this.numeroCredito = n; }

    public String getTipoCredito() { return tipoCredito; }
    public void setTipoCredito(String t) { this.tipoCredito = t; }

    public Double getMontoAprobado() { return montoAprobado; }
    public void setMontoAprobado(Double m) { this.montoAprobado = m; }

    public Double getSaldoCapital() { return saldoCapital; }
    public void setSaldoCapital(Double s) { this.saldoCapital = s; }

    public Integer getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(Integer p) { this.plazoMeses = p; }

    public Double getTasaInteresAnual() { return tasaInteresAnual; }
    public void setTasaInteresAnual(Double t) { this.tasaInteresAnual = t; }

    public Double getCuotaMensual() { return cuotaMensual; }
    public void setCuotaMensual(Double c) { this.cuotaMensual = c; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getDiasAtraso() { return diasAtraso; }
    public void setDiasAtraso(Integer d) { this.diasAtraso = d; }

    public Integer getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(Integer c) { this.cuotasPagadas = c; }

    public Integer getCuotasPendientes() { return cuotasPendientes; }
    public void setCuotasPendientes(Integer c) { this.cuotasPendientes = c; }

    public LocalDate getFechaDesembolso() { return fechaDesembolso; }
    public void setFechaDesembolso(LocalDate f) { this.fechaDesembolso = f; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }

    public LocalDate getFechaProximaCuota() { return fechaProximaCuota; }
    public void setFechaProximaCuota(LocalDate f) { this.fechaProximaCuota = f; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public SolicitudCredito getSolicitud() { return solicitud; }
    public void setSolicitud(SolicitudCredito s) { this.solicitud = s; }
}
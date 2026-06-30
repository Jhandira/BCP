package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos_credito")
public class PagoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double montoPagado;

    // Desglose del pago
    private Double montoCapital;
    private Double montoInteres;
    private Double montoMora;

    private Integer numeroCuota;

    // PUNTUAL, TARDIO
    private String tipoPago;

    private LocalDateTime fechaPago = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "credito_id", nullable = false)
    private Credito credito;

    @ManyToOne
    @JoinColumn(name = "cuenta_debito_id")
    private Cuenta cuentaDebito;

    // ── Getters y Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMontoPagado() { return montoPagado; }
    public void setMontoPagado(Double m) { this.montoPagado = m; }

    public Double getMontoCapital() { return montoCapital; }
    public void setMontoCapital(Double m) { this.montoCapital = m; }

    public Double getMontoInteres() { return montoInteres; }
    public void setMontoInteres(Double m) { this.montoInteres = m; }

    public Double getMontoMora() { return montoMora; }
    public void setMontoMora(Double m) { this.montoMora = m; }

    public Integer getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(Integer n) { this.numeroCuota = n; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String t) { this.tipoPago = t; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime f) { this.fechaPago = f; }

    public Credito getCredito() { return credito; }
    public void setCredito(Credito credito) { this.credito = credito; }

    public Cuenta getCuentaDebito() { return cuentaDebito; }
    public void setCuentaDebito(Cuenta c) { this.cuentaDebito = c; }
}
package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroCuenta;

    // AHORRO, CORRIENTE, PLAZO_FIJO
    @Column(nullable = false)
    private String tipoCuenta;

    // PEN, USD
    @Column(nullable = false)
    private String moneda = "PEN";

    @Column(nullable = false)
    private Double saldo = 0.0;

    // Tasa de interés anual (ej: 3.5 = 3.5%)
    private Double tasaInteresAnual = 0.0;

    // ACTIVA, BLOQUEADA, CERRADA
    @Column(nullable = false)
    private String estado = "ACTIVA";

    private LocalDateTime fechaApertura = LocalDateTime.now();
    private LocalDateTime fechaUltimaOperacion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // ── Getters y Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String n) { this.numeroCuenta = n; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String t) { this.tipoCuenta = t; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }

    public Double getTasaInteresAnual() { return tasaInteresAnual; }
    public void setTasaInteresAnual(Double t) { this.tasaInteresAnual = t; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime f) { this.fechaApertura = f; }

    public LocalDateTime getFechaUltimaOperacion() { return fechaUltimaOperacion; }
    public void setFechaUltimaOperacion(LocalDateTime f) { this.fechaUltimaOperacion = f; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}
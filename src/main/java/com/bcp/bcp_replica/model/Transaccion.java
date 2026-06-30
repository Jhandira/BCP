package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DEPOSITO, RETIRO, TRANSFERENCIA, PAGO_CREDITO, INTERES
    @Column(nullable = false)
    private String tipoTransaccion;

    @Column(nullable = false)
    private Double monto;

    private String descripcion;

    // Saldo después de la operación
    private Double saldoResultante;

    @Column(nullable = false)
    private LocalDateTime fechaTransaccion = LocalDateTime.now();

    // EXITOSA, FALLIDA, PENDIENTE
    @Column(nullable = false)
    private String estado = "EXITOSA";

    @ManyToOne
    @JoinColumn(name = "cuenta_origen_id")
    private Cuenta cuentaOrigen;

    @ManyToOne
    @JoinColumn(name = "cuenta_destino_id")
    private Cuenta cuentaDestino;

    // ── Getters y Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String t) { this.tipoTransaccion = t; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }

    public Double getSaldoResultante() { return saldoResultante; }
    public void setSaldoResultante(Double s) { this.saldoResultante = s; }

    public LocalDateTime getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(LocalDateTime f) { this.fechaTransaccion = f; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Cuenta getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(Cuenta c) { this.cuentaOrigen = c; }

    public Cuenta getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(Cuenta c) { this.cuentaDestino = c; }
}
package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_credito")
public class SolicitudCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo: CONSUMO, HIPOTECARIO, MYPE, CORPORATIVO, PERSONAL
    @Column(nullable = false)
    private String tipoCredito;

    @Column(nullable = false)
    private Double montoSolicitado;

    @Column(nullable = false)
    private Integer plazoMeses;

    // Motivo del préstamo
    private String proposito;

    // Scoring calculado automáticamente (0-100)
    private Integer scoringPuntaje;

    // Clasificación del scoring
    // EXCELENTE (80-100), BUENO (60-79), REGULAR (40-59), MALO (0-39)
    private String scoringClasificacion;

    // Estado del flujo de aprobación
    // PENDIENTE -> EN_REVISION -> APROBADO / RECHAZADO / EN_COMITE
    @Column(nullable = false)
    private String estado = "PENDIENTE";

    // Tasa de interés asignada (TEA %)
    private Double tasaInteresAnual;

    // Cuota mensual calculada
    private Double cuotaMensual;

    // Observaciones del asesor/comité
    private String observaciones;

    private LocalDateTime fechaSolicitud = LocalDateTime.now();
    private LocalDateTime fechaResolucion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "asesor_id")
    private Funcionario asesor;

    // ── Getters y Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoCredito() { return tipoCredito; }
    public void setTipoCredito(String t) { this.tipoCredito = t; }

    public Double getMontoSolicitado() { return montoSolicitado; }
    public void setMontoSolicitado(Double m) { this.montoSolicitado = m; }

    public Integer getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(Integer p) { this.plazoMeses = p; }

    public String getProposito() { return proposito; }
    public void setProposito(String p) { this.proposito = p; }

    public Integer getScoringPuntaje() { return scoringPuntaje; }
    public void setScoringPuntaje(Integer s) { this.scoringPuntaje = s; }

    public String getScoringClasificacion() { return scoringClasificacion; }
    public void setScoringClasificacion(String s) { this.scoringClasificacion = s; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Double getTasaInteresAnual() { return tasaInteresAnual; }
    public void setTasaInteresAnual(Double t) { this.tasaInteresAnual = t; }

    public Double getCuotaMensual() { return cuotaMensual; }
    public void setCuotaMensual(Double c) { this.cuotaMensual = c; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String o) { this.observaciones = o; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime f) { this.fechaSolicitud = f; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime f) { this.fechaResolucion = f; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Funcionario getAsesor() { return asesor; }
    public void setAsesor(Funcionario asesor) { this.asesor = asesor; }
}
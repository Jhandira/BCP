package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gestiones_cobranza")
public class GestionCobranza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipoGestion;

    @Column(nullable = false)
    private String descripcion;

    private String resultado;

    private LocalDateTime fechaGestion = LocalDateTime.now();
    private LocalDateTime fechaProximaAccion;

    @Column(nullable = false)
    private String estado = "COMPLETADA";

    @ManyToOne
    @JoinColumn(name = "credito_id", nullable = false)
    private Credito credito;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoGestion() { return tipoGestion; }
    public void setTipoGestion(String t) { this.tipoGestion = t; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }

    public String getResultado() { return resultado; }
    public void setResultado(String r) { this.resultado = r; }

    public LocalDateTime getFechaGestion() { return fechaGestion; }
    public void setFechaGestion(LocalDateTime f) { this.fechaGestion = f; }

    public LocalDateTime getFechaProximaAccion() { return fechaProximaAccion; }
    public void setFechaProximaAccion(LocalDateTime f) { this.fechaProximaAccion = f; }

    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }

    public Credito getCredito() { return credito; }
    public void setCredito(Credito c) { this.credito = c; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario f) { this.funcionario = f; }
}
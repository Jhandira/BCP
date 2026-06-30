package com.bcp.bcp_replica.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos personales
    @Column(nullable = false)
    private String nombreCompleto;

    @Column(unique = true, nullable = false)
    private String dni;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private String telefono;

    @Column(unique = true, nullable = false)
    private String email;

    // Datos laborales (para scoring)
    private String situacionLaboral;   // DEPENDIENTE, INDEPENDIENTE, DESEMPLEADO
    private Double ingresoMensual;
    private String empleador;

    // Datos de acceso
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol = "ROLE_CLIENTE";

    // Estado
    @Column(nullable = false)
    private Boolean activo = true;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // ── Getters y Setters ──────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String n) { this.nombreCompleto = n; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate f) { this.fechaNacimiento = f; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSituacionLaboral() { return situacionLaboral; }
    public void setSituacionLaboral(String s) { this.situacionLaboral = s; }

    public Double getIngresoMensual() { return ingresoMensual; }
    public void setIngresoMensual(Double i) { this.ingresoMensual = i; }

    public String getEmpleador() { return empleador; }
    public void setEmpleador(String e) { this.empleador = e; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }
}
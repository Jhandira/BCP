package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.SolicitudCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudCreditoRepository extends JpaRepository<SolicitudCredito, Long> {
    List<SolicitudCredito> findByClienteId(Long clienteId);
    List<SolicitudCredito> findByEstado(String estado);
    List<SolicitudCredito> findByAsesorId(Long asesorId);
    List<SolicitudCredito> findByEstadoOrderByFechaSolicitudAsc(String estado);
    long countByEstado(String estado);
}
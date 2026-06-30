package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.GestionCobranza;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GestionCobranzaRepository extends JpaRepository<GestionCobranza, Long> {
    List<GestionCobranza> findByCreditoIdOrderByFechaGestionDesc(Long creditoId);
    long countByCreditoId(Long creditoId);
}
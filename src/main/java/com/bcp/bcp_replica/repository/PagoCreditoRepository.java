package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.PagoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagoCreditoRepository extends JpaRepository<PagoCredito, Long> {
    List<PagoCredito> findByCreditoId(Long creditoId);
    List<PagoCredito> findByCreditoIdOrderByFechaPagoDesc(Long creditoId);
}

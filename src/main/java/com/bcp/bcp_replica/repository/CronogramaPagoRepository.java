package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.CronogramaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, Long> {
    List<CronogramaPago> findByCreditoIdOrderByNumeroCuotaAsc(Long creditoId);
    List<CronogramaPago> findByCreditoIdAndEstado(Long creditoId, String estado);
}
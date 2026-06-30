package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.Credito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CreditoRepository extends JpaRepository<Credito, Long> {
    List<Credito> findByClienteId(Long clienteId);
    List<Credito> findByEstado(String estado);
    List<Credito> findByDiasAtrasoGreaterThan(Integer dias);

    // Para indicadores gerenciales
    @Query("SELECT SUM(c.saldoCapital) FROM Credito c WHERE c.estado != 'CANCELADO' AND c.estado != 'CASTIGADO'")
    Double sumSaldoCapitalTotal();

    @Query("SELECT SUM(c.saldoCapital) FROM Credito c WHERE c.estado = 'ATRASADO' OR c.estado = 'JUDICIAL'")
    Double sumCarteraAtrasada();

    long countByEstado(String estado);

    // Para el módulo de mora
    @Query("SELECT c FROM Credito c WHERE c.diasAtraso > 0 ORDER BY c.diasAtraso DESC")
    List<Credito> findCreditosEnMora();

    @Query("SELECT COUNT(c) FROM Credito c WHERE c.diasAtraso > 0")
    long countCreditosEnMora();
}

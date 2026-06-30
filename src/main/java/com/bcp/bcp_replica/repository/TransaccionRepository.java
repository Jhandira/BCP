package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    @Query("SELECT t FROM Transaccion t WHERE t.cuentaOrigen.id = :cuentaId OR t.cuentaDestino.id = :cuentaId ORDER BY t.fechaTransaccion DESC")
    List<Transaccion> findByCuentaId(Long cuentaId);

    List<Transaccion> findByCuentaOrigenIdOrderByFechaTransaccionDesc(Long cuentaId);
}
package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsername(String username);
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByEmail(String email);
    boolean existsByDni(String dni);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}

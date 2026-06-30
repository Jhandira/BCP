package com.bcp.bcp_replica.repository;

import com.bcp.bcp_replica.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByUsername(String username);
    List<Funcionario> findByRol(String rol);
    boolean existsByUsername(String username);
}
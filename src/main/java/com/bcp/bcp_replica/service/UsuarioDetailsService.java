package com.bcp.bcp_replica.service;

import com.bcp.bcp_replica.model.Cliente;
import com.bcp.bcp_replica.model.Funcionario;
import com.bcp.bcp_replica.repository.ClienteRepository;
import com.bcp.bcp_replica.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscar primero en clientes
        Optional<Cliente> cliente = clienteRepository.findByUsername(username);
        if (cliente.isPresent()) {
            Cliente c = cliente.get();
            return new org.springframework.security.core.userdetails.User(
                    c.getUsername(),
                    c.getPassword(),
                    List.of(new SimpleGrantedAuthority(c.getRol()))
            );
        }

        // Buscar en funcionarios
        Optional<Funcionario> funcionario = funcionarioRepository.findByUsername(username);
        if (funcionario.isPresent()) {
            Funcionario f = funcionario.get();
            return new org.springframework.security.core.userdetails.User(
                    f.getUsername(),
                    f.getPassword(),
                    List.of(new SimpleGrantedAuthority(f.getRol()))
            );
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}
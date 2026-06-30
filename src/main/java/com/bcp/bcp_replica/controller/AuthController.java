package com.bcp.bcp_replica.controller;

import com.bcp.bcp_replica.model.Cliente;
import com.bcp.bcp_replica.repository.ClienteRepository;
import com.bcp.bcp_replica.service.JwtService;
import com.bcp.bcp_replica.service.UsuarioDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class AuthController {

    @Autowired private ClienteRepository clienteRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private UsuarioDetailsService userDetailsService;
    @Autowired private AuthenticationManager authenticationManager;

    @GetMapping("/")
    public String index() { return "index"; }

    // ── LOGIN CLIENTES ──────────────────────────────────────
    @GetMapping("/login")
    public String loginCliente(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registro,
            Model model) {
        if (error != null)    model.addAttribute("error", "Usuario o contraseña incorrectos.");
        if (logout != null)   model.addAttribute("logout", "Sesión cerrada correctamente.");
        if (registro != null) model.addAttribute("registro", true);
        return "login";
    }

    // ── POST LOGIN — genera JWT ─────────────────────────────
    @GetMapping("/post-login")
    public String postLogin(Authentication auth, HttpServletResponse response) {
        if (auth == null) return "redirect:/login";

        boolean esCliente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(auth.getName());
        String rol = auth.getAuthorities().iterator().next().getAuthority();
        String token = jwtService.generateToken(userDetails, rol);

        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return esCliente ? "redirect:/homebanking/dashboard" : "redirect:/core/dashboard";
    }

    // ── LOGIN FUNCIONARIOS ──────────────────────────────────
    @GetMapping("/core/login")
    public String loginCore(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null)  model.addAttribute("error", "Credenciales incorrectas.");
        if (logout != null) model.addAttribute("logout", "Sesión cerrada correctamente.");
        return "core-login";
    }

    // ── PROCESAR LOGIN FUNCIONARIOS ─────────────────────────
    @PostMapping("/core/login-process")
    public String procesarLoginCore(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            boolean esFuncionario = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ASESOR") ||
                            a.getAuthority().equals("ROLE_COMITE") ||
                            a.getAuthority().equals("ROLE_ADMINISTRADOR") ||
                            a.getAuthority().equals("ROLE_GERENTE"));

            if (!esFuncionario) {
                model.addAttribute("error", "Esta entrada es solo para funcionarios BCP.");
                return "core-login";
            }

            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String rol = auth.getAuthorities().iterator().next().getAuthority();
            String token = jwtService.generateToken(userDetails, rol);

            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            return "redirect:/core/dashboard";

        } catch (AuthenticationException e) {
            model.addAttribute("error", "Credenciales corporativas incorrectas.");
            return "core-login";
        }
    }

    // ── REGISTRO CLIENTES ───────────────────────────────────
    @GetMapping("/registro")
    public String registroForm() { return "registro"; }

    @PostMapping("/registro")
    public String registrar(
            @RequestParam String nombreCompleto,
            @RequestParam String dni,
            @RequestParam String fechaNacimiento,
            @RequestParam String telefono,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        if (clienteRepo.existsByUsername(username)) {
            model.addAttribute("error", "Ese nombre de usuario ya existe.");
            return "registro";
        }
        if (clienteRepo.existsByDni(dni)) {
            model.addAttribute("error", "Ese DNI ya está registrado.");
            return "registro";
        }
        if (clienteRepo.existsByEmail(email)) {
            model.addAttribute("error", "Ese email ya está registrado.");
            return "registro";
        }

        Cliente c = new Cliente();
        c.setNombreCompleto(nombreCompleto);
        c.setDni(dni);
        c.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setUsername(username);
        c.setPassword(passwordEncoder.encode(password));
        c.setRol("ROLE_CLIENTE");
        clienteRepo.save(c);

        return "redirect:/login?registro=true";
    }
}
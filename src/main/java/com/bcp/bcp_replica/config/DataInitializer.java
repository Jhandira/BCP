package com.bcp.bcp_replica.config;

import com.bcp.bcp_replica.model.*;
import com.bcp.bcp_replica.repository.*;
import com.bcp.bcp_replica.service.CreditoService;
import com.bcp.bcp_replica.service.ScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private FuncionarioRepository funcionarioRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private CuentaRepository cuentaRepo;
    @Autowired private CreditoRepository creditoRepo;
    @Autowired private SolicitudCreditoRepository solicitudRepo;
    @Autowired private CronogramaPagoRepository cronogramaRepo;
    @Autowired private TransaccionRepository transaccionRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CreditoService creditoService;
    @Autowired private ScoringService scoringService;

    @Override
    public void run(String... args) {
        crearFuncionarios();
        crearClientesYDatos();
        System.out.println("✅ Datos de prueba cargados correctamente.");
    }

    private void crearFuncionarios() {
        crearFuncionario("asesor01",  "Carlos Mendoza Ríos",   "ROLE_ASESOR",        "Asesor de Créditos", "12345601");
        crearFuncionario("comite01",  "María Torres Vega",     "ROLE_COMITE",        "Comité de Créditos", "12345602");
        crearFuncionario("admin01",   "Jorge Paredes Luna",    "ROLE_ADMINISTRADOR", "Administrador",      "12345603");
        crearFuncionario("gerente01", "Ana Castillo Herrera",  "ROLE_GERENTE",       "Gerente General",    "12345604");
    }

    private void crearFuncionario(String username, String nombre, String rol, String cargo, String dni) {
        if (!funcionarioRepo.existsByUsername(username)) {
            Funcionario f = new Funcionario();
            f.setUsername(username);
            f.setNombreCompleto(nombre);
            f.setPassword(passwordEncoder.encode("Bcp2025@"));
            f.setRol(rol);
            f.setCargo(cargo);
            f.setDni(dni);
            f.setEmail(username + "@bcp.com.pe");
            f.setTelefono("01-6000000");
            funcionarioRepo.save(f);
            System.out.println("✅ Funcionario: " + username);
        }
    }

    private void crearClientesYDatos() {
        // Cliente 1 — perfil excelente, crédito vigente
        if (!clienteRepo.existsByUsername("juan.perez")) {
            Cliente c1 = crearCliente(
                    "Juan Carlos Pérez García", "45678901",
                    LocalDate.of(1990, 3, 15), "987654321",
                    "juan.perez@gmail.com", "juan.perez",
                    "DEPENDIENTE", 5500.0, "Empresa ABC S.A.C."
            );
            Cuenta cuenta1 = crearCuenta(c1, "191-00000001-01", "AHORRO", "PEN", 8500.0, 3.5);
            Cuenta cuenta2 = crearCuenta(c1, "191-00000002-01", "CORRIENTE", "PEN", 2300.0, 0.0);
            crearTransaccion(cuenta1, "DEPOSITO", 3000.0, "Depósito de sueldo");
            crearTransaccion(cuenta1, "TRANSFERENCIA", 500.0, "Pago de servicios", cuenta2);
            crearCreditoAprobado(c1, "PERSONAL", 8000.0, 24, 0);
        }

        // Cliente 2 — mora temprana (15 días)
        if (!clienteRepo.existsByUsername("maria.lopez")) {
            Cliente c2 = crearCliente(
                    "María Elena López Soto", "45678902",
                    LocalDate.of(1985, 7, 22), "976543210",
                    "maria.lopez@hotmail.com", "maria.lopez",
                    "INDEPENDIENTE", 3200.0, "Negocio Propio"
            );
            Cuenta cuenta3 = crearCuenta(c2, "191-00000003-01", "AHORRO", "PEN", 1200.0, 3.5);
            crearTransaccion(cuenta3, "DEPOSITO", 1200.0, "Depósito inicial");
            crearCreditoConMora(c2, "CONSUMO", 5000.0, 12, 15);
        }

        // Cliente 3 — mora tardía (45 días)
        if (!clienteRepo.existsByUsername("pedro.silva")) {
            Cliente c3 = crearCliente(
                    "Pedro Antonio Silva Ruiz", "45678903",
                    LocalDate.of(1978, 11, 8), "965432109",
                    "pedro.silva@yahoo.com", "pedro.silva",
                    "DEPENDIENTE", 2800.0, "Municipalidad de Lima"
            );
            Cuenta cuenta4 = crearCuenta(c3, "191-00000004-01", "AHORRO", "PEN", 450.0, 3.5);
            crearTransaccion(cuenta4, "DEPOSITO", 450.0, "Depósito");
            crearCreditoConMora(c3, "PERSONAL", 12000.0, 36, 45);
        }

        // Cliente 4 — mora judicial (90 días)
        if (!clienteRepo.existsByUsername("lucia.quispe")) {
            Cliente c4 = crearCliente(
                    "Lucía Quispe Mamani", "45678904",
                    LocalDate.of(1992, 5, 30), "954321098",
                    "lucia.quispe@gmail.com", "lucia.quispe",
                    "INDEPENDIENTE", 1500.0, "Mercado San Pedro"
            );
            crearCreditoConMora(c4, "MYPE", 15000.0, 24, 90);
        }

        // Cliente 5 — solicitud pendiente hipotecario
        if (!clienteRepo.existsByUsername("rosa.garcia")) {
            Cliente c5 = crearCliente(
                    "Rosa Isabel García Torres", "45678905",
                    LocalDate.of(1995, 9, 12), "943210987",
                    "rosa.garcia@gmail.com", "rosa.garcia",
                    "DEPENDIENTE", 4200.0, "Hospital Nacional"
            );
            Cuenta cuenta6 = crearCuenta(c5, "191-00000006-01", "AHORRO", "PEN", 3800.0, 3.5);
            crearTransaccion(cuenta6, "DEPOSITO", 3800.0, "Depósito sueldo");
            crearSolicitudPendiente(c5, "HIPOTECARIO", 80000.0, 120);
        }

        // Cliente 6 — crédito castigado (200 días mora)
        if (!clienteRepo.existsByUsername("carlos.mamani")) {
            Cliente c6 = crearCliente(
                    "Carlos Eduardo Mamani Flores", "45678906",
                    LocalDate.of(1975, 2, 18), "932109876",
                    "carlos.mamani@outlook.com", "carlos.mamani",
                    "DESEMPLEADO", 800.0, null
            );
            crearCreditoConMora(c6, "CONSUMO", 3000.0, 12, 200);
        }
    }

    private Cliente crearCliente(String nombre, String dni, LocalDate fechaNac,
                                 String telefono, String email, String username,
                                 String situacion, Double ingreso, String empleador) {
        Cliente c = new Cliente();
        c.setNombreCompleto(nombre);
        c.setDni(dni);
        c.setFechaNacimiento(fechaNac);
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setUsername(username);
        c.setPassword(passwordEncoder.encode("Cliente2025@"));
        c.setRol("ROLE_CLIENTE");
        c.setSituacionLaboral(situacion);
        c.setIngresoMensual(ingreso);
        c.setEmpleador(empleador);
        return clienteRepo.save(c);
    }

    private Cuenta crearCuenta(Cliente cliente, String numero, String tipo,
                               String moneda, Double saldo, Double tasa) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(numero);
        cuenta.setTipoCuenta(tipo);
        cuenta.setMoneda(moneda);
        cuenta.setSaldo(saldo);
        cuenta.setTasaInteresAnual(tasa);
        cuenta.setCliente(cliente);
        return cuentaRepo.save(cuenta);
    }

    private void crearTransaccion(Cuenta origen, String tipo, Double monto, String desc) {
        Transaccion t = new Transaccion();
        t.setTipoTransaccion(tipo);
        t.setMonto(monto);
        t.setDescripcion(desc);
        t.setSaldoResultante(origen.getSaldo());
        t.setCuentaOrigen(origen);
        t.setFechaTransaccion(LocalDateTime.now().minusDays((long)(Math.random() * 30)));
        transaccionRepo.save(t);
    }

    private void crearTransaccion(Cuenta origen, String tipo, Double monto,
                                  String desc, Cuenta destino) {
        Transaccion t = new Transaccion();
        t.setTipoTransaccion(tipo);
        t.setMonto(monto);
        t.setDescripcion(desc);
        t.setSaldoResultante(origen.getSaldo());
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setFechaTransaccion(LocalDateTime.now().minusDays((long)(Math.random() * 15)));
        transaccionRepo.save(t);
    }

    private void crearCreditoAprobado(Cliente cliente, String tipo,
                                      Double monto, Integer plazo, Integer diasMora) {
        double tasa  = scoringService.obtenerTasaAnual(tipo, 80);
        double cuota = scoringService.calcularCuotaMensual(monto, plazo, tasa);

        Credito credito = new Credito();
        credito.setNumeroCredito("BCP-" + System.currentTimeMillis());
        credito.setTipoCredito(tipo);
        credito.setMontoAprobado(monto);
        credito.setSaldoCapital(monto * 0.85);
        credito.setPlazoMeses(plazo);
        credito.setTasaInteresAnual(tasa);
        credito.setCuotaMensual(cuota);
        credito.setEstado("VIGENTE");
        credito.setDiasAtraso(diasMora);
        credito.setCuotasPagadas(3);
        credito.setCuotasPendientes(plazo - 3);
        credito.setFechaDesembolso(LocalDate.now().minusMonths(3));
        credito.setFechaVencimiento(LocalDate.now().plusMonths(plazo - 3));
        credito.setFechaProximaCuota(LocalDate.now().plusMonths(1));
        credito.setCliente(cliente);
        Credito saved = creditoRepo.save(credito);
        creditoService.generarCronograma(saved);
    }

    private void crearCreditoConMora(Cliente cliente, String tipo,
                                     Double monto, Integer plazo, Integer diasMora) {
        double tasa  = scoringService.obtenerTasaAnual(tipo, 50);
        double cuota = scoringService.calcularCuotaMensual(monto, plazo, tasa);

        String estado = diasMora > 180 ? "CASTIGADO" :
                diasMora > 60  ? "JUDICIAL"  : "ATRASADO";

        Credito credito = new Credito();
        credito.setNumeroCredito("BCP-" + (System.currentTimeMillis() + diasMora));
        credito.setTipoCredito(tipo);
        credito.setMontoAprobado(monto);
        credito.setSaldoCapital(monto * 0.9);
        credito.setPlazoMeses(plazo);
        credito.setTasaInteresAnual(tasa);
        credito.setCuotaMensual(cuota);
        credito.setEstado(estado);
        credito.setDiasAtraso(diasMora);
        credito.setCuotasPagadas(1);
        credito.setCuotasPendientes(plazo - 1);
        credito.setFechaDesembolso(LocalDate.now().minusMonths(6));
        credito.setFechaVencimiento(LocalDate.now().plusMonths(plazo - 6));
        credito.setFechaProximaCuota(LocalDate.now().minusDays(diasMora));
        credito.setCliente(cliente);
        creditoRepo.save(credito);
    }

    private void crearSolicitudPendiente(Cliente cliente, String tipo,
                                         Double monto, Integer plazo) {
        SolicitudCredito sol = new SolicitudCredito();
        sol.setTipoCredito(tipo);
        sol.setMontoSolicitado(monto);
        sol.setPlazoMeses(plazo);
        sol.setProposito("Compra de vivienda propia");
        sol.setCliente(cliente);
        sol.setScoringPuntaje(72);
        sol.setScoringClasificacion("BUENO");
        sol.setEstado("PENDIENTE");
        solicitudRepo.save(sol);
    }
}
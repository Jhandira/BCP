package com.bcp.bcp_replica.controller;

import com.bcp.bcp_replica.model.*;
import com.bcp.bcp_replica.repository.*;
import com.bcp.bcp_replica.service.CreditoService;
import com.bcp.bcp_replica.service.ScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/core")
public class CoreController {

    @Autowired private FuncionarioRepository funcionarioRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private CuentaRepository cuentaRepo;
    @Autowired private CreditoRepository creditoRepo;
    @Autowired private SolicitudCreditoRepository solicitudRepo;
    @Autowired private CronogramaPagoRepository cronogramaRepo;
    @Autowired private ScoringService scoringService;
    @Autowired private CreditoService creditoService;

    private Funcionario getFuncionario(Authentication auth) {
        return funcionarioRepo.findByUsername(auth.getName()).orElse(null);
    }

    // ── DASHBOARD ───────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        Funcionario f = getFuncionario(auth);
        model.addAttribute("funcionario", f);

        long totalClientes     = clienteRepo.count();
        long totalCreditos     = creditoRepo.count();
        long solicitudesPend   = solicitudRepo.countByEstado("PENDIENTE");
        long creditosAtrasados = creditoRepo.countByEstado("ATRASADO");

        Double carteraTotal    = creditoRepo.sumSaldoCapitalTotal();
        Double carteraAtrasada = creditoRepo.sumCarteraAtrasada();

        double ratioMora = 0.0;
        if (carteraTotal != null && carteraTotal > 0 && carteraAtrasada != null) {
            ratioMora = (carteraAtrasada / carteraTotal) * 100;
        }

        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalCreditos", totalCreditos);
        model.addAttribute("solicitudesPendientes", solicitudesPend);
        model.addAttribute("creditosAtrasados", creditosAtrasados);
        model.addAttribute("carteraTotal", carteraTotal != null ? carteraTotal : 0.0);
        model.addAttribute("ratioMora", String.format("%.2f", ratioMora));
        model.addAttribute("solicitudes", solicitudRepo.findByEstadoOrderByFechaSolicitudAsc("PENDIENTE"));

        return "core/dashboard";
    }

    // ── SOLICITUDES ─────────────────────────────────────────
    @GetMapping("/solicitudes")
    public String solicitudes(Model model, Authentication auth) {
        model.addAttribute("funcionario", getFuncionario(auth));
        model.addAttribute("solicitudes", solicitudRepo.findAll());
        return "core/solicitudes";
    }

    @GetMapping("/solicitudes/{id}")
    public String verSolicitud(@PathVariable Long id, Model model, Authentication auth) {
        Funcionario f = getFuncionario(auth);
        SolicitudCredito sol = solicitudRepo.findById(id).orElseThrow();

        // Calcular RDS si tiene ingreso
        double rds = 0.0;
        String colorRds = "gray";
        String etiquetaRds = "Sin datos";
        String rutaAprobacion = creditoService.getRutaAprobacion(sol.getMontoSolicitado());
        boolean puedeAprobar = creditoService.puedeAprobar(sol.getMontoSolicitado(), f.getRol());

        if (sol.getCliente().getIngresoMensual() != null && sol.getCliente().getIngresoMensual() > 0) {
            // Calcular cuota estimada para el RDS
            double tasa = scoringService.obtenerTasaAnual(sol.getTipoCredito(),
                    sol.getScoringPuntaje() != null ? sol.getScoringPuntaje() : 50);
            double cuota = scoringService.calcularCuotaMensual(
                    sol.getMontoSolicitado(), sol.getPlazoMeses(), tasa);

            rds = creditoService.calcularRDS(cuota, sol.getCliente().getIngresoMensual());
            colorRds = creditoService.colorRDS(rds);
            etiquetaRds = creditoService.etiquetaRDS(rds);
        }

        model.addAttribute("funcionario", f);
        model.addAttribute("solicitud", sol);
        model.addAttribute("rds", String.format("%.1f", rds));
        model.addAttribute("colorRds", colorRds);
        model.addAttribute("etiquetaRds", etiquetaRds);
        model.addAttribute("rutaAprobacion", rutaAprobacion);
        model.addAttribute("puedeAprobar", puedeAprobar);

        return "core/solicitud-detalle";
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    public String aprobarSolicitud(@PathVariable Long id,
                                   @RequestParam String observaciones,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        SolicitudCredito sol = solicitudRepo.findById(id).orElseThrow();
        Funcionario f = getFuncionario(auth);

        // Verificar permiso por monto
        if (!creditoService.puedeAprobar(sol.getMontoSolicitado(), f.getRol())) {
            ra.addFlashAttribute("error",
                    "No tiene permisos para aprobar este monto. Ruta requerida: " +
                            creditoService.getRutaAprobacion(sol.getMontoSolicitado()));
            return "redirect:/core/solicitudes/" + id;
        }

        double tasa = scoringService.obtenerTasaAnual(sol.getTipoCredito(),
                sol.getScoringPuntaje() != null ? sol.getScoringPuntaje() : 50);
        double cuota = scoringService.calcularCuotaMensual(
                sol.getMontoSolicitado(), sol.getPlazoMeses(), tasa);

        sol.setEstado("APROBADO");
        sol.setObservaciones(observaciones);
        sol.setAsesor(f);
        sol.setFechaResolucion(LocalDateTime.now());
        sol.setTasaInteresAnual(tasa);
        sol.setCuotaMensual(cuota);
        solicitudRepo.save(sol);

        // Crear crédito
        Credito credito = new Credito();
        credito.setNumeroCredito("BCP-" + System.currentTimeMillis());
        credito.setTipoCredito(sol.getTipoCredito());
        credito.setMontoAprobado(sol.getMontoSolicitado());
        credito.setSaldoCapital(sol.getMontoSolicitado());
        credito.setPlazoMeses(sol.getPlazoMeses());
        credito.setTasaInteresAnual(tasa);
        credito.setCuotaMensual(cuota);
        credito.setCuotasPendientes(sol.getPlazoMeses());
        credito.setFechaDesembolso(LocalDate.now());
        credito.setFechaVencimiento(LocalDate.now().plusMonths(sol.getPlazoMeses()));
        credito.setFechaProximaCuota(LocalDate.now().plusMonths(1));
        credito.setCliente(sol.getCliente());
        credito.setSolicitud(sol);
        creditoRepo.save(credito);

        // Generar cronograma automáticamente
        creditoService.generarCronograma(credito);

        ra.addFlashAttribute("mensaje", "Crédito aprobado y cronograma generado correctamente.");
        return "redirect:/core/solicitudes";
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    public String rechazarSolicitud(@PathVariable Long id,
                                    @RequestParam String observaciones,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        SolicitudCredito sol = solicitudRepo.findById(id).orElseThrow();
        Funcionario f = getFuncionario(auth);
        sol.setEstado("RECHAZADO");
        sol.setObservaciones(observaciones);
        sol.setAsesor(f);
        sol.setFechaResolucion(LocalDateTime.now());
        solicitudRepo.save(sol);
        ra.addFlashAttribute("mensaje", "Solicitud rechazada.");
        return "redirect:/core/solicitudes";
    }

    // ── CLIENTES ────────────────────────────────────────────
    @GetMapping("/clientes")
    public String clientes(Model model, Authentication auth) {
        model.addAttribute("funcionario", getFuncionario(auth));
        model.addAttribute("clientes", clienteRepo.findAll());
        return "core/clientes";
    }

    @GetMapping("/clientes/{id}")
    public String verCliente(@PathVariable Long id, Model model, Authentication auth) {
        model.addAttribute("funcionario", getFuncionario(auth));
        Cliente cliente = clienteRepo.findById(id).orElseThrow();
        model.addAttribute("cliente", cliente);
        model.addAttribute("cuentas", cuentaRepo.findByClienteId(id));
        model.addAttribute("creditos", creditoRepo.findByClienteId(id));
        model.addAttribute("solicitudes", solicitudRepo.findByClienteId(id));
        return "core/cliente-detalle";
    }

    // ── CRÉDITOS ────────────────────────────────────────────
    @GetMapping("/creditos")
    public String creditos(Model model, Authentication auth) {
        model.addAttribute("funcionario", getFuncionario(auth));
        model.addAttribute("creditos", creditoRepo.findAll());
        return "core/creditos";
    }

    // ── REPORTES ────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reportes(Model model, Authentication auth) {
        model.addAttribute("funcionario", getFuncionario(auth));

        Double carteraTotal    = creditoRepo.sumSaldoCapitalTotal();
        Double carteraAtrasada = creditoRepo.sumCarteraAtrasada();
        long totalClientes     = clienteRepo.count();
        long totalCreditos     = creditoRepo.count();
        long aprobados         = solicitudRepo.countByEstado("APROBADO");
        long rechazados        = solicitudRepo.countByEstado("RECHAZADO");
        long pendientes        = solicitudRepo.countByEstado("PENDIENTE");

        double ratioMora = 0.0;
        if (carteraTotal != null && carteraTotal > 0 && carteraAtrasada != null) {
            ratioMora = (carteraAtrasada / carteraTotal) * 100;
        }

        model.addAttribute("carteraTotal", carteraTotal != null ? carteraTotal : 0.0);
        model.addAttribute("carteraAtrasada", carteraAtrasada != null ? carteraAtrasada : 0.0);
        model.addAttribute("ratioMora", String.format("%.2f", ratioMora));
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalCreditos", totalCreditos);
        model.addAttribute("aprobados", aprobados);
        model.addAttribute("rechazados", rechazados);
        model.addAttribute("pendientes", pendientes);

        return "core/reportes";
    }
}
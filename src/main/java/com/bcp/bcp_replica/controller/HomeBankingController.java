package com.bcp.bcp_replica.controller;

import com.bcp.bcp_replica.model.*;
import com.bcp.bcp_replica.repository.*;
import com.bcp.bcp_replica.service.ScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/homebanking")
public class HomeBankingController {

    @Autowired private ClienteRepository clienteRepo;
    @Autowired private CuentaRepository cuentaRepo;
    @Autowired private TransaccionRepository transaccionRepo;
    @Autowired private CreditoRepository creditoRepo;
    @Autowired private SolicitudCreditoRepository solicitudRepo;
    @Autowired private ScoringService scoringService;
    @Autowired private CronogramaPagoRepository cronogramaRepo;

    private Cliente getCliente(Authentication auth) {
        return clienteRepo.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        Cliente cliente = getCliente(auth);
        List<Cuenta> cuentas = cuentaRepo.findByClienteId(cliente.getId());
        List<Credito> creditos = creditoRepo.findByClienteId(cliente.getId());

        double saldoTotal = cuentas.stream()
                .filter(c -> "ACTIVA".equals(c.getEstado()))
                .mapToDouble(Cuenta::getSaldo)
                .sum();

        List<Transaccion> ultimasTransacciones = cuentas.stream()
                .flatMap(c -> transaccionRepo.findByCuentaOrigenIdOrderByFechaTransaccionDesc(c.getId()).stream())
                .sorted((a, b) -> b.getFechaTransaccion().compareTo(a.getFechaTransaccion()))
                .limit(5)
                .toList();

        model.addAttribute("cliente", cliente);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("creditos", creditos);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("ultimasTransacciones", ultimasTransacciones);
        return "homebanking/dashboard";
    }


    @GetMapping("/cuentas")
    public String cuentas(Model model, Authentication auth) {
        Cliente cliente = getCliente(auth);
        model.addAttribute("cliente", cliente);
        model.addAttribute("cuentas", cuentaRepo.findByClienteId(cliente.getId()));
        return "homebanking/cuentas";
    }

    @PostMapping("/cuentas/abrir")
    public String abrirCuenta(@RequestParam String tipoCuenta,
                              @RequestParam String moneda,
                              Authentication auth,
                              RedirectAttributes ra) {
        Cliente cliente = getCliente(auth);
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setTipoCuenta(tipoCuenta);
        cuenta.setMoneda(moneda);
        cuenta.setSaldo(0.0);
        cuenta.setTasaInteresAnual(tipoCuenta.equals("AHORRO") ? 3.5 :
                tipoCuenta.equals("PLAZO_FIJO") ? 6.0 : 0.0);
        cuenta.setCliente(cliente);
        cuentaRepo.save(cuenta);
        ra.addFlashAttribute("mensaje", "Cuenta " + tipoCuenta + " abierta exitosamente.");
        return "redirect:/homebanking/cuentas";
    }

    @GetMapping("/transferencias")
    public String transferencias(Model model, Authentication auth) {
        Cliente cliente = getCliente(auth);
        List<Cuenta> cuentas = cuentaRepo.findByClienteIdAndEstado(cliente.getId(), "ACTIVA");
        model.addAttribute("cliente", cliente);
        model.addAttribute("cuentas", cuentas);
        return "homebanking/transferencias";
    }

    @PostMapping("/transferencias/realizar")
    public String realizarTransferencia(
            @RequestParam Long cuentaOrigenId,
            @RequestParam String numeroCuentaDestino,
            @RequestParam Double monto,
            @RequestParam(required = false) String descripcion,
            Authentication auth,
            RedirectAttributes ra) {

        Cliente cliente = getCliente(auth);
        Cuenta origen = cuentaRepo.findById(cuentaOrigenId).orElseThrow();

        if (!origen.getCliente().getId().equals(cliente.getId())) {
            ra.addFlashAttribute("error", "Cuenta de origen no válida.");
            return "redirect:/homebanking/transferencias";
        }

        if (origen.getSaldo() < monto) {
            ra.addFlashAttribute("error", "Saldo insuficiente. Saldo disponible: S/ " +
                    String.format("%.2f", origen.getSaldo()));
            return "redirect:/homebanking/transferencias";
        }

        Cuenta destino = cuentaRepo.findByNumeroCuenta(numeroCuentaDestino).orElse(null);
        if (destino == null) {
            ra.addFlashAttribute("error", "Cuenta destino no encontrada: " + numeroCuentaDestino);
            return "redirect:/homebanking/transferencias";
        }

        origen.setSaldo(origen.getSaldo() - monto);
        destino.setSaldo(destino.getSaldo() + monto);
        origen.setFechaUltimaOperacion(LocalDateTime.now());
        destino.setFechaUltimaOperacion(LocalDateTime.now());
        cuentaRepo.save(origen);
        cuentaRepo.save(destino);

        Transaccion t = new Transaccion();
        t.setTipoTransaccion("TRANSFERENCIA");
        t.setMonto(monto);
        t.setDescripcion(descripcion != null ? descripcion : "Transferencia BCP");
        t.setSaldoResultante(origen.getSaldo());
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        transaccionRepo.save(t);

        ra.addFlashAttribute("mensaje",
                "Transferencia de S/ " + String.format("%.2f", monto) + " realizada exitosamente.");
        return "redirect:/homebanking/transferencias";
    }

    @GetMapping("/movimientos/{cuentaId}")
    public String movimientos(@PathVariable Long cuentaId, Model model, Authentication auth) {
        Cliente cliente = getCliente(auth);
        Cuenta cuenta = cuentaRepo.findById(cuentaId).orElseThrow();
        if (!cuenta.getCliente().getId().equals(cliente.getId())) {
            return "redirect:/homebanking/dashboard";
        }
        List<Transaccion> movimientos = transaccionRepo.findByCuentaId(cuentaId);
        model.addAttribute("cliente", cliente);
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("movimientos", movimientos);
        return "homebanking/movimientos";
    }

    @GetMapping("/creditos/solicitar")
    public String solicitarCreditoForm(Model model, Authentication auth) {
        Cliente cliente = getCliente(auth);
        model.addAttribute("cliente", cliente);
        return "homebanking/solicitar-credito";
    }

    @PostMapping("/creditos/solicitar")
    public String solicitarCredito(
            @RequestParam String tipoCredito,
            @RequestParam Double montoSolicitado,
            @RequestParam Integer plazoMeses,
            @RequestParam(required = false) String proposito,
            @RequestParam(required = false) String situacionLaboral,
            @RequestParam(required = false) Double ingresoMensual,
            @RequestParam(required = false) String empleador,
            Authentication auth,
            RedirectAttributes ra) {

        Cliente cliente = getCliente(auth);
        if (situacionLaboral != null) cliente.setSituacionLaboral(situacionLaboral);
        if (ingresoMensual != null)   cliente.setIngresoMensual(ingresoMensual);
        if (empleador != null)        cliente.setEmpleador(empleador);
        clienteRepo.save(cliente);

        SolicitudCredito sol = new SolicitudCredito();
        sol.setTipoCredito(tipoCredito);
        sol.setMontoSolicitado(montoSolicitado);
        sol.setPlazoMeses(plazoMeses);
        sol.setProposito(proposito);
        sol.setCliente(cliente);

        int puntaje = scoringService.calcularScoring(cliente, sol);
        sol.setScoringPuntaje(puntaje);
        sol.setScoringClasificacion(scoringService.clasificarScoring(puntaje));
        solicitudRepo.save(sol);

        ra.addFlashAttribute("mensaje",
                "Solicitud enviada. Tu scoring es: " + puntaje + " - " +
                        scoringService.clasificarScoring(puntaje) + ". Un asesor la revisará pronto.");
        return "redirect:/homebanking/creditos";
    }

    @GetMapping("/creditos")
    public String misCreditos(Model model, Authentication auth) {
        Cliente cliente = getCliente(auth);
        model.addAttribute("cliente", cliente);
        model.addAttribute("creditos", creditoRepo.findByClienteId(cliente.getId()));
        model.addAttribute("solicitudes", solicitudRepo.findByClienteId(cliente.getId()));
        return "homebanking/creditos";
    }
    @GetMapping("/creditos/{id}/cronograma")
    public String verCronograma(@PathVariable Long id, Model model, Authentication auth) {
        Cliente cliente = getCliente(auth);
        Credito credito = creditoRepo.findById(id).orElseThrow();
        if (!credito.getCliente().getId().equals(cliente.getId())) {
            return "redirect:/homebanking/creditos";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("credito", credito);
        model.addAttribute("cronograma", cronogramaRepo.findByCreditoIdOrderByNumeroCuotaAsc(id));
        return "homebanking/cronograma";
    }

    private String generarNumeroCuenta() {

        Random r = new Random();
        return String.format("191-%08d-%02d", r.nextInt(99999999), r.nextInt(99));
    }
}
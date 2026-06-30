package com.bcp.bcp_replica.controller;

import com.bcp.bcp_replica.model.*;
import com.bcp.bcp_replica.repository.*;
import com.bcp.bcp_replica.service.MoraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/core/recuperaciones")
public class RecuperacionesController {

    @Autowired private CreditoRepository creditoRepo;
    @Autowired private GestionCobranzaRepository gestionRepo;
    @Autowired private FuncionarioRepository funcionarioRepo;
    @Autowired private MoraService moraService;

    private Funcionario getFuncionario(Authentication auth) {
        return funcionarioRepo.findByUsername(auth.getName()).orElseThrow();
    }

    @GetMapping
    public String consultaMora(Model model, Authentication auth) {
        Funcionario f = getFuncionario(auth);
        model.addAttribute("funcionario", f);
        moraService.actualizarDiasAtraso();

        MoraService.KpisMora kpis = moraService.calcularKpis();
        model.addAttribute("kpis", kpis);

        List<Credito> todos = creditoRepo.findAll();
        model.addAttribute("preventiva", todos.stream().filter(c -> moraService.getBandaMora(c.getDiasAtraso()).equals("PREVENTIVA")).toList());
        model.addAttribute("temprana",   todos.stream().filter(c -> moraService.getBandaMora(c.getDiasAtraso()).equals("TEMPRANA")).toList());
        model.addAttribute("tardia",     todos.stream().filter(c -> moraService.getBandaMora(c.getDiasAtraso()).equals("TARDIA")).toList());
        model.addAttribute("judicial",   todos.stream().filter(c -> moraService.getBandaMora(c.getDiasAtraso()).equals("JUDICIAL")).toList());
        model.addAttribute("castigo",    todos.stream().filter(c -> moraService.getBandaMora(c.getDiasAtraso()).equals("CASTIGO")).toList());

        return "core/recuperaciones/consulta";
    }

    @GetMapping("/credito/{id}")
    public String detalleCredito(@PathVariable Long id, Model model, Authentication auth) {
        Funcionario f = getFuncionario(auth);
        Credito credito = creditoRepo.findById(id).orElseThrow();
        List<GestionCobranza> gestiones = gestionRepo.findByCreditoIdOrderByFechaGestionDesc(id);

        String banda = moraService.getBandaMora(credito.getDiasAtraso());
        double mora  = moraService.calcularMoraDiaria(credito);

        model.addAttribute("funcionario", f);
        model.addAttribute("credito", credito);
        model.addAttribute("gestiones", gestiones);
        model.addAttribute("banda", banda);
        model.addAttribute("colorBanda", moraService.getColorSemaforo(banda));
        model.addAttribute("montoMora", mora);
        model.addAttribute("puedeDerivarse", moraService.puedeDerivarse(credito, f.getRol()));
        model.addAttribute("puedeCastigarse", moraService.puedeCastigarse(credito, f.getRol()));

        return "core/recuperaciones/detalle";
    }

    @PostMapping("/credito/{id}/gestion")
    public String registrarGestion(@PathVariable Long id,
                                   @RequestParam String tipoGestion,
                                   @RequestParam String descripcion,
                                   @RequestParam String resultado,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        Funcionario f = getFuncionario(auth);
        Credito credito = creditoRepo.findById(id).orElseThrow();

        GestionCobranza g = new GestionCobranza();
        g.setTipoGestion(tipoGestion);
        g.setDescripcion(descripcion);
        g.setResultado(resultado);
        g.setCredito(credito);
        g.setFuncionario(f);
        gestionRepo.save(g);

        ra.addFlashAttribute("mensaje", "Gestión registrada correctamente.");
        return "redirect:/core/recuperaciones/credito/" + id;
    }

    @PostMapping("/credito/{id}/judicial")
    public String derivarJudicial(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Funcionario f = getFuncionario(auth);
        Credito credito = creditoRepo.findById(id).orElseThrow();

        if (!moraService.puedeDerivarse(credito, f.getRol())) {
            ra.addFlashAttribute("error", "No tiene permisos o el crédito no cumple el umbral de 61 días.");
            return "redirect:/core/recuperaciones/credito/" + id;
        }

        credito.setEstado("JUDICIAL");
        creditoRepo.save(credito);

        GestionCobranza g = new GestionCobranza();
        g.setTipoGestion("DERIVACION_JUDICIAL");
        g.setDescripcion("Crédito derivado a cobranza judicial. Días de atraso: " + credito.getDiasAtraso());
        g.setResultado("DERIVADO");
        g.setCredito(credito);
        g.setFuncionario(f);
        gestionRepo.save(g);

        ra.addFlashAttribute("mensaje", "Crédito derivado a cobranza judicial.");
        return "redirect:/core/recuperaciones/credito/" + id;
    }

    @PostMapping("/credito/{id}/castigar")
    public String castigarCredito(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Funcionario f = getFuncionario(auth);
        Credito credito = creditoRepo.findById(id).orElseThrow();

        if (!moraService.puedeCastigarse(credito, f.getRol())) {
            ra.addFlashAttribute("error", "Solo el Gerente puede castigar créditos con más de 180 días de atraso.");
            return "redirect:/core/recuperaciones/credito/" + id;
        }

        credito.setEstado("CASTIGADO");
        creditoRepo.save(credito);

        GestionCobranza g = new GestionCobranza();
        g.setTipoGestion("CASTIGO");
        g.setDescripcion("Crédito castigado contablemente. Días: " + credito.getDiasAtraso() +
                " | Saldo: S/ " + String.format("%.2f", credito.getSaldoCapital()));
        g.setResultado("CASTIGADO");
        g.setCredito(credito);
        g.setFuncionario(f);
        gestionRepo.save(g);

        ra.addFlashAttribute("mensaje", "Crédito castigado contablemente.");
        return "redirect:/core/recuperaciones";
    }
}
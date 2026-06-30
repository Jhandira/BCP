package com.bcp.bcp_replica.service;

import com.bcp.bcp_replica.model.Credito;
import com.bcp.bcp_replica.repository.CreditoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MoraService {

    @Autowired
    private CreditoRepository creditoRepo;

    public String getBandaMora(int diasAtraso) {
        if (diasAtraso == 0)   return "NORMAL";
        if (diasAtraso <= 8)   return "PREVENTIVA";
        if (diasAtraso <= 30)  return "TEMPRANA";
        if (diasAtraso <= 60)  return "TARDIA";
        if (diasAtraso <= 180) return "JUDICIAL";
        return "CASTIGO";
    }

    public String getColorSemaforo(String banda) {
        return switch (banda) {
            case "NORMAL"     -> "green";
            case "PREVENTIVA" -> "yellow";
            case "TEMPRANA"   -> "orange";
            case "TARDIA"     -> "red";
            case "JUDICIAL"   -> "darkred";
            case "CASTIGO"    -> "black";
            default           -> "gray";
        };
    }

    public double calcularMoraDiaria(Credito credito) {
        if (credito.getDiasAtraso() == 0) return 0.0;
        double tasaMoraDiaria = 0.0003;
        return credito.getSaldoCapital() * tasaMoraDiaria * credito.getDiasAtraso();
    }

    public boolean puedeDerivarse(Credito credito, String rol) {
        return credito.getDiasAtraso() >= 61 &&
                (rol.equals("ROLE_ADMINISTRADOR") || rol.equals("ROLE_GERENTE"));
    }

    public boolean puedeCastigarse(Credito credito, String rol) {
        return credito.getDiasAtraso() > 180 && rol.equals("ROLE_GERENTE");
    }

    public void actualizarDiasAtraso() {
        List<Credito> creditos = creditoRepo.findByEstado("VIGENTE");
        LocalDate hoy = LocalDate.now();
        for (Credito c : creditos) {
            if (c.getFechaProximaCuota() != null && c.getFechaProximaCuota().isBefore(hoy)) {
                long dias = ChronoUnit.DAYS.between(c.getFechaProximaCuota(), hoy);
                c.setDiasAtraso((int) dias);
                if (dias > 0) c.setEstado("ATRASADO");
                creditoRepo.save(c);
            }
        }
    }

    public record KpisMora(
            long totalCreditos,
            long enMora,
            long preventiva,
            long temprana,
            long tardia,
            long judicial,
            long castigo,
            double porcentajeMora
    ) {}

    public KpisMora calcularKpis() {
        List<Credito> todos = creditoRepo.findAll();
        long total  = todos.size();
        long enMora = todos.stream().filter(c -> c.getDiasAtraso() > 0).count();
        long prev   = todos.stream().filter(c -> getBandaMora(c.getDiasAtraso()).equals("PREVENTIVA")).count();
        long temp   = todos.stream().filter(c -> getBandaMora(c.getDiasAtraso()).equals("TEMPRANA")).count();
        long tard   = todos.stream().filter(c -> getBandaMora(c.getDiasAtraso()).equals("TARDIA")).count();
        long jud    = todos.stream().filter(c -> getBandaMora(c.getDiasAtraso()).equals("JUDICIAL")).count();
        long cast   = todos.stream().filter(c -> getBandaMora(c.getDiasAtraso()).equals("CASTIGO")).count();
        double pct  = total > 0 ? (enMora * 100.0 / total) : 0.0;
        return new KpisMora(total, enMora, prev, temp, tard, jud, cast, pct);
    }
}
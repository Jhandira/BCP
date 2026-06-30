package com.bcp.bcp_replica.service;

import com.bcp.bcp_replica.model.*;
import com.bcp.bcp_replica.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreditoService {

    @Autowired private CronogramaPagoRepository cronogramaRepo;
    @Autowired private CreditoRepository creditoRepo;

    /**
     * Genera el cronograma de pagos usando sistema francés (cuota fija)
     * Se llama automáticamente al aprobar una solicitud
     */
    public void generarCronograma(Credito credito) {
        double monto       = credito.getMontoAprobado();
        int    plazo       = credito.getPlazoMeses();
        double tasaAnual   = credito.getTasaInteresAnual();
        double tasaMensual = tasaAnual / 100.0 / 12.0;

        double saldo = monto;
        LocalDate fechaInicio = credito.getFechaDesembolso() != null
                ? credito.getFechaDesembolso() : LocalDate.now();

        List<CronogramaPago> cronograma = new ArrayList<>();

        for (int i = 1; i <= plazo; i++) {
            double interesCuota = saldo * tasaMensual;
            double capitalCuota = credito.getCuotaMensual() - interesCuota;

            // Ajuste última cuota por redondeo
            if (i == plazo) {
                capitalCuota = saldo;
            }

            saldo = Math.max(0, saldo - capitalCuota);

            CronogramaPago cuota = new CronogramaPago();
            cuota.setNumeroCuota(i);
            cuota.setFechaVencimiento(fechaInicio.plusMonths(i));
            cuota.setCuotaTotal(redondear(interesCuota + capitalCuota));
            cuota.setCapital(redondear(capitalCuota));
            cuota.setInteres(redondear(interesCuota));
            cuota.setSaldoRestante(redondear(saldo));
            cuota.setEstado("PENDIENTE");
            cuota.setCredito(credito);

            cronograma.add(cuota);
        }

        cronogramaRepo.saveAll(cronograma);
    }

    /**
     * RDS — Ratio Deuda/Salario
     * Norma BCP: la cuota no debe superar el 30% del ingreso neto
     * Semáforo:
     *   VERDE  : RDS <= 25%
     *   AMARILLO: RDS 26-35%
     *   ROJO   : RDS > 35%
     */
    public double calcularRDS(double cuotaMensual, double ingresoMensual) {
        if (ingresoMensual <= 0) return 0.0;
        return (cuotaMensual / ingresoMensual) * 100.0;
    }

    public String colorRDS(double rds) {
        if (rds <= 25.0) return "green";
        if (rds <= 35.0) return "yellow";
        return "red";
    }

    public String etiquetaRDS(double rds) {
        if (rds <= 25.0) return "BAJO RIESGO";
        if (rds <= 35.0) return "RIESGO MODERADO";
        return "ALTO RIESGO";
    }

    /**
     * Ruta de aprobación por monto:
     * <= 10,000     → ASESOR puede aprobar
     * 10,001-50,000 → ADMINISTRADOR o superior
     * > 50,000      → COMITÉ (gerente)
     */
    public String getRutaAprobacion(double monto) {
        if (monto <= 10000)  return "ASESOR";
        if (monto <= 50000)  return "ADMINISTRADOR";
        return "COMITE";
    }

    public boolean puedeAprobar(double monto, String rol) {
        String ruta = getRutaAprobacion(monto);
        return switch (ruta) {
            case "ASESOR" -> true; // cualquier funcionario
            case "ADMINISTRADOR" -> rol.equals("ROLE_ADMINISTRADOR") || rol.equals("ROLE_GERENTE");
            case "COMITE" -> rol.equals("ROLE_GERENTE");
            default -> false;
        };
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
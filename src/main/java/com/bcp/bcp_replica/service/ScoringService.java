package com.bcp.bcp_replica.service;

import com.bcp.bcp_replica.model.Cliente;
import com.bcp.bcp_replica.model.Credito;
import com.bcp.bcp_replica.model.SolicitudCredito;
import com.bcp.bcp_replica.repository.CreditoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Scoring crediticio básico inspirado en el modelo del BCP.
 * Puntaje de 0 a 100 basado en:
 *   - Ingresos mensuales vs cuota solicitada
 *   - Situación laboral
 *   - Historial crediticio (créditos en mora)
 *   - Monto solicitado vs ingresos
 */
@Service
public class ScoringService {

    @Autowired
    private CreditoRepository creditoRepository;

    public int calcularScoring(Cliente cliente, SolicitudCredito solicitud) {
        int puntaje = 0;

        // 1. CAPACIDAD DE PAGO (40 puntos max)
        // Cuota no debe superar el 30% del ingreso mensual (regla BCP)
        if (cliente.getIngresoMensual() != null && cliente.getIngresoMensual() > 0) {
            double cuotaEstimada = calcularCuotaEstimada(
                    solicitud.getMontoSolicitado(),
                    solicitud.getPlazoMeses(),
                    obtenerTasaMensual(solicitud.getTipoCredito())
            );
            double ratioEndeudamiento = cuotaEstimada / cliente.getIngresoMensual();

            if (ratioEndeudamiento <= 0.20) puntaje += 40;
            else if (ratioEndeudamiento <= 0.25) puntaje += 35;
            else if (ratioEndeudamiento <= 0.30) puntaje += 25;
            else if (ratioEndeudamiento <= 0.40) puntaje += 10;
            // > 40% = 0 puntos
        }

        // 2. SITUACIÓN LABORAL (25 puntos max)
        if (cliente.getSituacionLaboral() != null) {
            switch (cliente.getSituacionLaboral()) {
                case "DEPENDIENTE"    -> puntaje += 25;
                case "INDEPENDIENTE"  -> puntaje += 15;
                case "PENSIONISTA"    -> puntaje += 20;
                default               -> puntaje += 0;
            }
        }

        // 3. HISTORIAL CREDITICIO (25 puntos max)
        List<Credito> creditosCliente = creditoRepository.findByClienteId(cliente.getId());
        if (creditosCliente.isEmpty()) {
            // Sin historial: puntaje neutral
            puntaje += 15;
        } else {
            long creditosAtrasados = creditosCliente.stream()
                    .filter(c -> "ATRASADO".equals(c.getEstado()))
                    .count();
            long creditosCancelados = creditosCliente.stream()
                    .filter(c -> "CANCELADO".equals(c.getEstado()))
                    .count();

            if (creditosAtrasados == 0 && creditosCancelados > 0) puntaje += 25; // historial limpio
            else if (creditosAtrasados == 0) puntaje += 20;
            else if (creditosAtrasados == 1) puntaje += 10;
            else puntaje += 0; // múltiples atrasos
        }

        // 4. NIVEL DE INGRESOS ABSOLUTO (10 puntos max)
        if (cliente.getIngresoMensual() != null) {
            if (cliente.getIngresoMensual() >= 5000)      puntaje += 10;
            else if (cliente.getIngresoMensual() >= 3000) puntaje += 7;
            else if (cliente.getIngresoMensual() >= 1500) puntaje += 4;
            else                                           puntaje += 1;
        }

        return Math.min(puntaje, 100);
    }

    public String clasificarScoring(int puntaje) {
        if (puntaje >= 80) return "EXCELENTE";
        if (puntaje >= 60) return "BUENO";
        if (puntaje >= 40) return "REGULAR";
        return "MALO";
    }

    /**
     * Tasa de interés anual (TEA) según tipo de crédito
     * Basado en tasas reales del BCP
     */
    public double obtenerTasaAnual(String tipoCredito, int scoringPuntaje) {
        double tasaBase = switch (tipoCredito) {
            case "CONSUMO"      -> 35.0;
            case "HIPOTECARIO"  -> 9.5;
            case "MYPE"         -> 28.0;
            case "CORPORATIVO"  -> 12.0;
            case "PERSONAL"     -> 30.0;
            default             -> 35.0;
        };

        // Descuento por buen scoring
        if (scoringPuntaje >= 80) tasaBase -= 5.0;
        else if (scoringPuntaje >= 60) tasaBase -= 2.0;
        else if (scoringPuntaje < 40) tasaBase += 5.0;

        return tasaBase;
    }

    /**
     * Cálculo de cuota mensual con sistema francés (cuota fija)
     * Fórmula: C = P * [r(1+r)^n] / [(1+r)^n - 1]
     */
    public double calcularCuotaMensual(double monto, int plazoMeses, double tasaAnual) {
        double tasaMensual = tasaAnual / 100.0 / 12.0;
        if (tasaMensual == 0) return monto / plazoMeses;
        double factor = Math.pow(1 + tasaMensual, plazoMeses);
        return monto * (tasaMensual * factor) / (factor - 1);
    }

    private double calcularCuotaEstimada(double monto, int plazoMeses, double tasaMensual) {
        if (tasaMensual == 0) return monto / plazoMeses;
        double factor = Math.pow(1 + tasaMensual, plazoMeses);
        return monto * (tasaMensual * factor) / (factor - 1);
    }

    private double obtenerTasaMensual(String tipoCredito) {
        double tasaAnual = switch (tipoCredito) {
            case "CONSUMO"     -> 35.0;
            case "HIPOTECARIO" -> 9.5;
            case "MYPE"        -> 28.0;
            case "CORPORATIVO" -> 12.0;
            default            -> 35.0;
        };
        return tasaAnual / 100.0 / 12.0;
    }
}
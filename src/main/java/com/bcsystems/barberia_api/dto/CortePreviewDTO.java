package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CortePreviewDTO {
    private Double saldoInicial;
    private Double totalVentas;
    private Double totalVentasContado;
    private Double totalVentasCredito;
    private Double totalIngresos;
    private Double totalEgresos;
    private Double saldoEsperado;
    private Double saldoActual;
    private Double diferencia;
}

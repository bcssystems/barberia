package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoCajaDTO {
    private Integer idMovimiento;
    private Integer idCaja;
    private String nombreCaja;
    private String tipo;
    private Double monto;
    private String motivo;
    private LocalDateTime fecha;
    private LocalDateTime fechaCorte;
    private Integer idVenta;
}

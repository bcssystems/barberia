package com.bcsystems.barberia_api.dto;

import com.bcsystems.barberia_api.domain.en.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VentaPagoDTO {
    private Integer idVentaPago;
    private Integer idVenta;
    private MetodoPago metodoPago;
    private Double monto;
    private String referencia;
    private LocalDateTime fecha;
}

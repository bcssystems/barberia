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
public class DetalleComisionDTO {
    private Integer idVentaDetalle;
    private Integer idVenta;
    private Integer idServicio;
    private String nombreServicio;
    private Double precioServicio;
    private Double montoComision;
    private LocalDateTime fechaVenta;
    private Boolean pagada;
}
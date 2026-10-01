package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MetodoPagoResumenDTO {
    private String metodo;
    private Integer cantidadOperaciones;
    private Double total;
}

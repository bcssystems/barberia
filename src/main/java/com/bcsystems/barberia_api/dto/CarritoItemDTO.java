package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarritoItemDTO {
    private Integer idCarritoItem;
    private Integer idCaja;
    private String tipo;
    private Integer idReferencia;
    private String nombre;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}

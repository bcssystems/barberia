package com.bcsystems.barberia_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO {
    private Integer idProducto;
    private String nombre;
    private String sku;
    private String descripcion;
    private Double precioCompra;
    private Double precioVenta;
    private Integer stock;
    private Integer status;
    private Boolean pagoCaja;

    @JsonIgnore
    public Integer getId() {
        return idProducto;
    }
}
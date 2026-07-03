package com.bcsystems.barberia_api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;
    @Column(unique = true)
    private String nombre;
    @Column(unique = true, nullable = false)
    private String sku;
    private String descripcion;
    private Double precioCompra;
    private Double precioVenta;
    private Integer stock;
    private Integer status = 1;

}

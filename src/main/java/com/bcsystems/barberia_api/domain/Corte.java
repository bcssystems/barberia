package com.bcsystems.barberia_api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cortes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Corte {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCorte;
    
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    
    // Ventas
    private Double totalVentasServicios;
    private Double totalVentasProductos;
    private Double totalVentas;
    
    // Costos
    private Double costoProductosVendidos;
    private Double gastosInventario;
    private Double totalCostos;
    
    // Utilidad bruta
    private Double utilidadBruta;
    
    // Comisiones
    private Double totalComisionesPendientes;
    private Double totalComisionesPagadas;
    
    // Utilidad neta
    private Double utilidadNeta;
    
    // Fecha en que se guardo el corte
    private LocalDateTime fechaRegistro;
    
    // Estado: ACTIVO, CANCELADO
    private String estado = "ACTIVO";
}

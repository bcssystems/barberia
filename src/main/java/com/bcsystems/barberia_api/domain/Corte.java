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

    // Caja
    private Double totalFondoCaja = 0.0;
    private Double totalIngresosCaja = 0.0;
    private Double totalEgresosCaja = 0.0;
    private Double saldoEsperado = 0.0;
    private Double saldoFinal = 0.0;

    // Desglose por metodo de pago del corte
    private Double totalEfectivo = 0.0;
    private Double totalTarjeta = 0.0;
    private Double totalTransferencia = 0.0;

    // Caja que realizo el corte
    private Integer idCaja;
    private String nombreCaja;

    // Usuario (cajero) que realizo el corte
    private String usuario;

    // Operaciones por metodo de pago segun el sistema
    private Integer efectivoOperaciones = 0;
    private Integer tarjetaOperaciones = 0;
    private Integer transferenciaOperaciones = 0;

    // Conteo real declarado por el cajero (null si no se capturo)
    private Double efectivoReal;
    private Double tarjetaReal;
    private Double transferenciaReal;
    private Double totalReal;
    private Double diferencia;
    
    // Fecha en que se guardo el corte
    private LocalDateTime fechaRegistro;
    
    // Estado: ACTIVO, CANCELADO
    private String estado = "ACTIVO";
}

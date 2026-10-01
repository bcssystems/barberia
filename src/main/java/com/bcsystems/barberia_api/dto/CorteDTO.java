package com.bcsystems.barberia_api.dto;

import com.bcsystems.barberia_api.domain.Corte;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Vista de solo lectura de un corte. Evita exponer la entidad
 * y evita que el cliente pueda enviar importes arbitrarios.
 */
@Getter
@Setter
@NoArgsConstructor
public class CorteDTO {

    private Integer idCorte;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private Double totalVentasServicios;
    private Double totalVentasProductos;
    private Double totalVentas;

    private Double costoProductosVendidos;
    private Double gastosInventario;
    private Double totalCostos;

    private Double utilidadBruta;

    private Double totalComisionesPendientes;
    private Double totalComisionesPagadas;
    private Double utilidadNeta;

    private Double totalFondoCaja;
    private Double totalIngresosCaja;
    private Double totalEgresosCaja;
    private Double saldoEsperado;
    private Double saldoFinal;

    private Double totalEfectivo;
    private Double totalTarjeta;
    private Double totalTransferencia;

    private Integer idCaja;
    private String nombreCaja;
    private String usuario;

    private Integer efectivoOperaciones;
    private Integer tarjetaOperaciones;
    private Integer transferenciaOperaciones;

    private Double efectivoReal;
    private Double tarjetaReal;
    private Double transferenciaReal;
    private Double totalReal;
    private Double diferencia;

    private LocalDateTime fechaRegistro;
    private String estado;

    public static CorteDTO from(Corte corte) {
        if (corte == null) return null;
        CorteDTO dto = new CorteDTO();
        dto.idCorte = corte.getIdCorte();
        dto.fechaInicio = corte.getFechaInicio();
        dto.fechaFin = corte.getFechaFin();
        dto.totalVentasServicios = corte.getTotalVentasServicios();
        dto.totalVentasProductos = corte.getTotalVentasProductos();
        dto.totalVentas = corte.getTotalVentas();
        dto.costoProductosVendidos = corte.getCostoProductosVendidos();
        dto.gastosInventario = corte.getGastosInventario();
        dto.totalCostos = corte.getTotalCostos();
        dto.utilidadBruta = corte.getUtilidadBruta();
        dto.totalComisionesPendientes = corte.getTotalComisionesPendientes();
        dto.totalComisionesPagadas = corte.getTotalComisionesPagadas();
        dto.utilidadNeta = corte.getUtilidadNeta();
        dto.totalFondoCaja = corte.getTotalFondoCaja();
        dto.totalIngresosCaja = corte.getTotalIngresosCaja();
        dto.totalEgresosCaja = corte.getTotalEgresosCaja();
        dto.saldoEsperado = corte.getSaldoEsperado();
        dto.saldoFinal = corte.getSaldoFinal();
        dto.totalEfectivo = corte.getTotalEfectivo();
        dto.totalTarjeta = corte.getTotalTarjeta();
        dto.totalTransferencia = corte.getTotalTransferencia();
        dto.idCaja = corte.getIdCaja();
        dto.nombreCaja = corte.getNombreCaja();
        dto.usuario = corte.getUsuario();
        dto.efectivoOperaciones = corte.getEfectivoOperaciones();
        dto.tarjetaOperaciones = corte.getTarjetaOperaciones();
        dto.transferenciaOperaciones = corte.getTransferenciaOperaciones();
        dto.efectivoReal = corte.getEfectivoReal();
        dto.tarjetaReal = corte.getTarjetaReal();
        dto.transferenciaReal = corte.getTransferenciaReal();
        dto.totalReal = corte.getTotalReal();
        dto.diferencia = corte.getDiferencia();
        dto.fechaRegistro = corte.getFechaRegistro();
        dto.estado = corte.getEstado();
        return dto;
    }
}

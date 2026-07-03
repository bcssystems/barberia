package com.bcsystems.barberia_api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CorteCompletoDTO {
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    
    // Ventas
    private Double totalVentasServicios;
    private Double totalVentasProductos;
    private Double totalVentas;
    
    // Costos
    private Double costoProductosVendidos;
    private Double gastosInventario; // Compras de inventario nuevo
    private Double totalCostos;
    
    // Utilidad bruta
    private Double utilidadBruta;
    
    // Comisiones
    private Double totalComisionesPendientes;
    private Double totalComisionesPagadas;
    private List<ResumenComisionEmpleadoDTO> comisionesPorEmpleado;
    
    // Utilidad neta
    private Double utilidadNeta;

    // Caja
    private Double totalFondoCaja;
    private Double totalIngresosCaja;
    private Double totalEgresosCaja;
    private Double saldoEsperado;
    private Double saldoFinal;
    
    // Getters y setters
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    
    public Double getTotalVentasServicios() { return totalVentasServicios; }
    public void setTotalVentasServicios(Double totalVentasServicios) { this.totalVentasServicios = totalVentasServicios; }
    
    public Double getTotalVentasProductos() { return totalVentasProductos; }
    public void setTotalVentasProductos(Double totalVentasProductos) { this.totalVentasProductos = totalVentasProductos; }
    
    public Double getTotalVentas() { return totalVentas; }
    public void setTotalVentas(Double totalVentas) { this.totalVentas = totalVentas; }
    
    public Double getCostoProductosVendidos() { return costoProductosVendidos; }
    public void setCostoProductosVendidos(Double costoProductosVendidos) { this.costoProductosVendidos = costoProductosVendidos; }
    
    public Double getGastosInventario() { return gastosInventario; }
    public void setGastosInventario(Double gastosInventario) { this.gastosInventario = gastosInventario; }
    
    public Double getTotalCostos() { return totalCostos; }
    public void setTotalCostos(Double totalCostos) { this.totalCostos = totalCostos; }
    
    public Double getUtilidadBruta() { return utilidadBruta; }
    public void setUtilidadBruta(Double utilidadBruta) { this.utilidadBruta = utilidadBruta; }
    
    public Double getTotalComisionesPendientes() { return totalComisionesPendientes; }
    public void setTotalComisionesPendientes(Double totalComisionesPendientes) { this.totalComisionesPendientes = totalComisionesPendientes; }
    
    public Double getTotalComisionesPagadas() { return totalComisionesPagadas; }
    public void setTotalComisionesPagadas(Double totalComisionesPagadas) { this.totalComisionesPagadas = totalComisionesPagadas; }
    
    public List<ResumenComisionEmpleadoDTO> getComisionesPorEmpleado() { return comisionesPorEmpleado; }
    public void setComisionesPorEmpleado(List<ResumenComisionEmpleadoDTO> comisionesPorEmpleado) { this.comisionesPorEmpleado = comisionesPorEmpleado; }
    
    public Double getUtilidadNeta() { return utilidadNeta; }
    public void setUtilidadNeta(Double utilidadNeta) { this.utilidadNeta = utilidadNeta; }

    public Double getTotalFondoCaja() { return totalFondoCaja; }
    public void setTotalFondoCaja(Double totalFondoCaja) { this.totalFondoCaja = totalFondoCaja; }

    public Double getTotalIngresosCaja() { return totalIngresosCaja; }
    public void setTotalIngresosCaja(Double totalIngresosCaja) { this.totalIngresosCaja = totalIngresosCaja; }

    public Double getTotalEgresosCaja() { return totalEgresosCaja; }
    public void setTotalEgresosCaja(Double totalEgresosCaja) { this.totalEgresosCaja = totalEgresosCaja; }

    public Double getSaldoEsperado() { return saldoEsperado; }
    public void setSaldoEsperado(Double saldoEsperado) { this.saldoEsperado = saldoEsperado; }

    public Double getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(Double saldoFinal) { this.saldoFinal = saldoFinal; }
}

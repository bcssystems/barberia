package com.bcsystems.barberia_api.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CorteComisionDTO {
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Double totalComisionesPendientes;
    private Double totalComisionesPagadas;
    private List<ResumenComisionEmpleadoDTO> comisionesPorEmpleado;

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public Double getTotalComisionesPendientes() { return totalComisionesPendientes; }
    public void setTotalComisionesPendientes(Double totalComisionesPendientes) { this.totalComisionesPendientes = totalComisionesPendientes; }

    public Double getTotalComisionesPagadas() { return totalComisionesPagadas; }
    public void setTotalComisionesPagadas(Double totalComisionesPagadas) { this.totalComisionesPagadas = totalComisionesPagadas; }

    public List<ResumenComisionEmpleadoDTO> getComisionesPorEmpleado() { return comisionesPorEmpleado; }
    public void setComisionesPorEmpleado(List<ResumenComisionEmpleadoDTO> comisionesPorEmpleado) { this.comisionesPorEmpleado = comisionesPorEmpleado; }
}

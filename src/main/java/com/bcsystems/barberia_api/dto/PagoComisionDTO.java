package com.bcsystems.barberia_api.dto;

import com.bcsystems.barberia_api.domain.en.EstadoPago;

import java.time.LocalDateTime;

public class PagoComisionDTO {
    private Integer idPagoComision;
    private Integer idEmpleado;
    private String nombreEmpleado;
    private Double montoComision;
    private LocalDateTime fechaCorteInicio;
    private LocalDateTime fechaCorteFin;
    private LocalDateTime fechaPago;
    private EstadoPago estado;
    private Boolean deleted;

    public Integer getIdPagoComision() { return idPagoComision; }
    public void setIdPagoComision(Integer idPagoComision) { this.idPagoComision = idPagoComision; }

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public Double getMontoComision() { return montoComision; }
    public void setMontoComision(Double montoComision) { this.montoComision = montoComision; }

    public LocalDateTime getFechaCorteInicio() { return fechaCorteInicio; }
    public void setFechaCorteInicio(LocalDateTime fechaCorteInicio) { this.fechaCorteInicio = fechaCorteInicio; }

    public LocalDateTime getFechaCorteFin() { return fechaCorteFin; }
    public void setFechaCorteFin(LocalDateTime fechaCorteFin) { this.fechaCorteFin = fechaCorteFin; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}

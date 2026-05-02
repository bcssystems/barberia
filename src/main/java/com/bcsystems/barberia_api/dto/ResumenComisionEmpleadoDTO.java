package com.bcsystems.barberia_api.dto;

public class ResumenComisionEmpleadoDTO {
    private Integer idEmpleado;
    private String nombreEmpleado;
    private Double totalVentasServicios;
    private Double porcentajeComision;
    private Double montoComision;
    private Boolean tienePagoPendiente;

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public Double getTotalVentasServicios() { return totalVentasServicios; }
    public void setTotalVentasServicios(Double totalVentasServicios) { this.totalVentasServicios = totalVentasServicios; }

    public Double getPorcentajeComision() { return porcentajeComision; }
    public void setPorcentajeComision(Double porcentajeComision) { this.porcentajeComision = porcentajeComision; }

    public Double getMontoComision() { return montoComision; }
    public void setMontoComision(Double montoComision) { this.montoComision = montoComision; }

    public Boolean getTienePagoPendiente() { return tienePagoPendiente; }
    public void setTienePagoPendiente(Boolean tienePagoPendiente) { this.tienePagoPendiente = tienePagoPendiente; }
}

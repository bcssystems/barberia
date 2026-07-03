package com.bcsystems.barberia_api.dto;

import java.util.List;

public class ResumenComisionEmpleadoDTO {
    private Integer idEmpleado;
    private String nombreEmpleado;
    private Double totalVentasServicios;
    private Double montoComision;
    private Boolean tienePagoPendiente;
    private List<DetalleComisionDTO> desglose;

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public Double getTotalVentasServicios() { return totalVentasServicios; }
    public void setTotalVentasServicios(Double totalVentasServicios) { this.totalVentasServicios = totalVentasServicios; }

    public Double getMontoComision() { return montoComision; }
    public void setMontoComision(Double montoComision) { this.montoComision = montoComision; }

    public Boolean getTienePagoPendiente() { return tienePagoPendiente; }
    public void setTienePagoPendiente(Boolean tienePagoPendiente) { this.tienePagoPendiente = tienePagoPendiente; }

    public List<DetalleComisionDTO> getDesglose() { return desglose; }
    public void setDesglose(List<DetalleComisionDTO> desglose) { this.desglose = desglose; }
}

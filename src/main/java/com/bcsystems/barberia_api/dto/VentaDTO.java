package com.bcsystems.barberia_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VentaDTO {
    private Integer idVenta;
    private Integer idCita;
    private Integer idCaja;
    private String nombreCaja;
    private String nombreCliente;
    private Integer idEmpleado;
    private String nombreEmpleado;
    private LocalDateTime fecha;
    private String tipoVenta;
    private Double subtotal;
    private Double descuento;
    private Double total;
    private Boolean cancelada;
    private List<VentaDetalleDTO> detalles;
    private List<VentaPagoDTO> pagos;
    private LocalDateTime fechaCorte;

    @JsonIgnore
    public Integer getId() {
        return idVenta;
    }
}
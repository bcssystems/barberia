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
    private String nombreCliente;
    private String nombreEmpleado;
    private LocalDateTime fecha;
    private Double total;
    private List<VentaDetalleDTO> detalles;

    @JsonIgnore
    public Integer getId() {
        return idVenta;
    }
}
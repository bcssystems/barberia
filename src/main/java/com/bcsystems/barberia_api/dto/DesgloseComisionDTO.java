package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DesgloseComisionDTO {
    private Integer idEmpleado;
    private String nombreEmpleado;
    private List<DetalleComisionDTO> detalles;
    private Double totalComision;
}

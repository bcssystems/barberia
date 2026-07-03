package com.bcsystems.barberia_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmpleadoDTO {
    private Integer idEmpleado;
    private String nombre;
    private String telefono;
    private Double sueldoBase;
    private Boolean cobraComision;
    private Integer status;

    @JsonIgnore
    public Integer getId() {
        return idEmpleado;
    }
}
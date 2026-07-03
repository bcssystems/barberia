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
public class ServicioDTO {
    private Integer idServicio;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private Double precio;
    private Double comision;
    private Integer status;

    @JsonIgnore
    public Integer getId() {
        return idServicio;
    }
}
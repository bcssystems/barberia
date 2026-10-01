package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermisoDTO {
    private Integer idPermiso;
    private String clave;
    private String nombre;
    private String descripcion;
    private String modulo;
    private Boolean activo;
}

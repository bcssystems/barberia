package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RolDTO {
    private Integer idRol;
    private String nombre;
    private String descripcion;
    private Boolean esSistema;
    private Boolean activo;
    private List<PermisoDTO> permisos = new ArrayList<>();
}

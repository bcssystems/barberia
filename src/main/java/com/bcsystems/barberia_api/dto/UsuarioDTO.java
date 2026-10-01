package com.bcsystems.barberia_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class UsuarioDTO {
    private Integer idUsuario;
    private String nombre;
    private String usuario;

    // Solo se acepta al crear o cambiar la contraseña, nunca se devuelve en las respuestas
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private RolDTO rol;
    private Integer idRol;
    private Boolean activo;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;
    private List<String> permisos;
}

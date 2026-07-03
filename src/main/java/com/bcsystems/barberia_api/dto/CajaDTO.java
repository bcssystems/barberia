package com.bcsystems.barberia_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CajaDTO {
    private Integer idCaja;
    private String nombre;
    private Double saldoInicial;
    private Double saldoActual;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private String estado;
}

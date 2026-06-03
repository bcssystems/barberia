package com.bcsystems.barberia_api.dto;

import com.bcsystems.barberia_api.domain.en.TipoMovimiento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoInventarioDTO {
    private Integer idMovimientoInventario;
    private Integer idProducto;
    private String nombreProducto;
    private Integer cantidad;
    private TipoMovimiento tipoMovimiento;
    private LocalDateTime fecha;
    private String motivo;
    private Boolean pagoCaja;

    @JsonIgnore
    public Integer getId() {
        return idMovimientoInventario;
    }
}
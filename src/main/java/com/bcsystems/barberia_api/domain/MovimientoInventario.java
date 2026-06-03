package com.bcsystems.barberia_api.domain;

import com.bcsystems.barberia_api.domain.en.TipoMovimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimientoInventario;
    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;
    private Integer cantidad;
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipoMovimiento;
    private LocalDateTime fecha = LocalDateTime.now();
    private String motivo;
    private Boolean pagoCaja = false;
    private LocalDateTime fechaCorte;

}

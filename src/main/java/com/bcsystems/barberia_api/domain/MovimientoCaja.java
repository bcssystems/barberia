package com.bcsystems.barberia_api.domain;

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
@Table(name = "movimiento_caja")
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @ManyToOne
    @JoinColumn(name = "id_caja")
    private Caja caja;

    private String tipo;

    private Double monto;

    private String motivo;

    private LocalDateTime fecha = LocalDateTime.now();

    private LocalDateTime fechaCorte;

    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = true)
    private Venta venta;
}

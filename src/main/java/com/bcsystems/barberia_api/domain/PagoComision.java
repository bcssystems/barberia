package com.bcsystems.barberia_api.domain;

import com.bcsystems.barberia_api.domain.en.EstadoPago;
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
@Table(name = "pago_comision")
public class PagoComision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPagoComision;

    @ManyToOne
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;

    private Double montoComision;

    private LocalDateTime fechaCorteInicio;

    private LocalDateTime fechaCorteFin;

    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    private Boolean deleted = false;

}

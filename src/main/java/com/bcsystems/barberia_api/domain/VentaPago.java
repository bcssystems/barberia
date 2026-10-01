package com.bcsystems.barberia_api.domain;

import com.bcsystems.barberia_api.domain.en.MetodoPago;
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
@Table(name = "venta_pago")
public class VentaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVentaPago;

    @ManyToOne
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private Double monto;

    private String referencia;

    private LocalDateTime fecha = LocalDateTime.now();

}

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
@Table(name = "carrito_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_caja", "tipo", "id_referencia"}))
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCarritoItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_caja", nullable = false)
    private Caja caja;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "id_referencia", nullable = false)
    private Integer idReferencia;

    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(nullable = false)
    private Double precioUnitario = 0.0;

    @Column(nullable = false)
    private LocalDateTime fechaAgregado = LocalDateTime.now();

}

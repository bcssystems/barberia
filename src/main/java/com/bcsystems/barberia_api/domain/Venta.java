package com.bcsystems.barberia_api.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVenta;
    @ManyToOne
    @JoinColumn(name = "id_cita", nullable = true)
    private Cita cita;
    @ManyToOne
    @JoinColumn(name = "id_caja", nullable = true)
    private Caja caja;
    @ManyToOne
    @JoinColumn(name = "id_empleado", nullable = true)
    private Empleado empleado;
    private LocalDateTime fecha = LocalDateTime.now();
    private String tipoVenta = "CONTADO";
    private Double subtotal = 0.0;
    private Double descuento = 0.0;
    private Double total;
    private Boolean cancelada = false;
    private LocalDateTime fechaCorte;
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaDetalle> detalles = new ArrayList<>();
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaPago> pagos = new ArrayList<>();

}

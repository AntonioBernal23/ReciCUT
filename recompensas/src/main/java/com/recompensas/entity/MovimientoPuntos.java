package com.recompensas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_puntos")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoPuntos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 100)
    private String referencia;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
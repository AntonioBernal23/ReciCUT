package com.campania_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(
        name = "inscripciones",
        uniqueConstraints = @UniqueConstraint(columnNames = {"campania_id", "usuarioId"})
)
@Getter
@Setter
@NoArgsConstructor
public class InscripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campania_id")
    private CampaniaEntity campania;

    @Column(nullable = false)
    private Long usuarioId;

    private Date fechaInscripcion;
}
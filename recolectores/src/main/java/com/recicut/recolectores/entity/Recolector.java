package com.recicut.recolectores.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recolectores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recolector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String zona;
    private Long usuarioId;
}
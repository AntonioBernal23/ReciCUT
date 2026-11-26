package com.campania_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CampaniaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String name;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    private Date dateStart;

    private Date endDate;

    private Date createdAt;

    private Integer puntos;

    public enum Type {
        VOLUNTARIA, SERVICIO
    }

    public enum Estado {
        ACTIVA, VENCIDA, BORRADOR, CREADA
    }
}
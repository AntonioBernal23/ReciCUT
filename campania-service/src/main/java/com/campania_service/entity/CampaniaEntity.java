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

    private Type type;

    private Estado estado;

    private Date dateStart;

    private Date endDate;

    private Date createdAt;

    public enum Type {
        VOLUNTARIA, SERVICIO
    }

    public enum Estado{
        ACTIVA, VENCIDA, BORRADOR, CREADA
    }

    public CampaniaEntity(Long userId, String name, Type type, Estado estado, Date dateStart, Date endDate, Date createdAt) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.estado = estado;
        this.dateStart = dateStart;
        this.endDate = endDate;
        this.createdAt = createdAt;
    }
}

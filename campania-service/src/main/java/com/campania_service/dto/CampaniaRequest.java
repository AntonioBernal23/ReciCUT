package com.campania_service.dto;

import com.campania_service.entity.CampaniaEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CampaniaRequest {

    private Long userId;

    private String name;

    private CampaniaEntity.Type type;

    private CampaniaEntity.Estado estado;

    private Date dateStart;

    private Date endDate;

    private Date createdAt;

    public enum Type {
        VOLUNTARIA, SERVICIO
    }

    public enum Estado{
        ACTIVA, VENCIDA, BORRADOR, CREADA
    }

}

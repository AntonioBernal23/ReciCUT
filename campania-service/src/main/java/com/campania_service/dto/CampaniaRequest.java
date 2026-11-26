package com.campania_service.dto;

import com.campania_service.entity.CampaniaEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotNull(message = "El tipo es obligatorio")
    private CampaniaEntity.Type type;

    private CampaniaEntity.Estado estado;

    private Date dateStart;

    private Date endDate;

    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    private Integer puntos;
}
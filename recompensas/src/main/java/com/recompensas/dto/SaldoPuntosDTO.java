package com.recompensas.dto;

public class SaldoPuntosDTO {

    private Long usuarioId;
    private Integer puntos;

    public SaldoPuntosDTO() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }
}
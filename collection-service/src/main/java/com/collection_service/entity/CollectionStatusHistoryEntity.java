package com.collection_service.entity;

import com.collection_service.enums.CollectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Bitacora: un renglon por cada cambio de estado de una solicitud.
 *
 * Responde la pregunta que siempre acaba surgiendo: "¿quien cancelo esto
 * y por que?". Tambien permite medir cuanto se tarda cada etapa.
 *
 * Esto se llama audit trail y es un patron real de sistemas en produccion.
 * Vale la pena mencionarlo en la exposicion.
 */
@Entity
@Table(
        name = "collection_status_history",
        indexes = @Index(name = "idx_csh_request", columnList = "request_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private CollectionRequestEntity request;

    /** Estado anterior. Nulo en el primer renglon (la creacion). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CollectionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollectionStatus toStatus;

    /** Quien hizo el cambio. Del JWT. */
    @Column(nullable = false)
    private Long changedByUserId;

    /** Motivo, cuando aplica (cancelacion, rechazo). */
    @Column(length = 255)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    void alCrear() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }
}

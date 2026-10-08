package com.collection_service.entity;

import com.collection_service.enums.CollectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Una solicitud de recoleccion: un usuario pide que pasen por su material.
 * Es la tabla principal del servicio.
 */
@Entity
@Table(
        name = "collection_request",
        indexes = {
                @Index(name = "idx_cr_requester", columnList = "requesterId"),
                @Index(name = "idx_cr_status",    columnList = "status"),
                @Index(name = "idx_cr_collector", columnList = "collectorId"),
                @Index(name = "idx_cr_completed", columnList = "completedAt")
        }
)
@Getter
@Setter
@NoArgsConstructor      // JPA lo EXIGE: Hibernate crea el objeto vacio y luego llena
@AllArgsConstructor
@Builder
public class CollectionRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTO_INCREMENT de MySQL
    private Long id;

    // ---------------------------------------------------------------
    // Referencias a OTROS microservicios: Long pelado, sin @ManyToOne.
    // Viven en otra base de datos, no se puede poner llave foranea.
    // ---------------------------------------------------------------

    /** Quien pide la recoleccion. Viene del JWT (auth-service). */
    @Column(nullable = false)
    private Long requesterId;

    /** Quien recoge. De collector-service. Nulo hasta que se asigna. */
    private Long collectorId;

    /** A donde va el material. De center-service. Opcional. */
    private Long recyclingCenterId;

    // ---------------------------------------------------------------
    // Datos propios de la solicitud
    // ---------------------------------------------------------------

    @Enumerated(EnumType.STRING)    // guarda "PENDING", no 0  <-- ver nota abajo
    @Column(nullable = false, length = 20)
    private CollectionStatus status;

    /** Donde recoger dentro del campus. Ej: "Edificio C, planta baja". */
    @Column(nullable = false, length = 255)
    private String pickupLocation;

    /** Detalle fino. Ej: "junto a la maquina de refrescos". */
    @Column(length = 500)
    private String pickupDetails;

    /** Notas del solicitante. */
    @Column(length = 500)
    private String notes;

    /** Cuando quiere que pasen. */
    private LocalDateTime scheduledAt;

    // ---------------------------------------------------------------
    // Marcas de tiempo de cada etapa.
    // Guardar una por etapa (en vez de un solo updatedAt) permite calcular
    // metricas: cuanto tardamos en asignar, cuanto dura una recoleccion.
    // report-service se los va a agradecer.
    // ---------------------------------------------------------------

    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    /** Cuando se cancelo o rechazo. */
    private LocalDateTime closedAt;

    /** Motivo de la cancelacion o el rechazo. */
    @Column(length = 255)
    private String closeReason;

    /**
     * Suma de los pesos reales de los items. Se calcula UNA vez, al completar.
     *
     * BigDecimal y no double: en double, 0.1 + 0.2 da 0.30000000000000004.
     * Para cantidades que un humano va a sumar o comparar, siempre BigDecimal.
     * precision = 8, scale = 3  ->  hasta 99999.999 kg
     */
    @Column(precision = 8, scale = 3)
    private BigDecimal totalWeightKg;

    // ---------------------------------------------------------------
    // Relacion con los items. Esta SI lleva @OneToMany porque ambas tablas
    // viven en la MISMA base de datos (recicut_collection).
    // ---------------------------------------------------------------

    @OneToMany(
            mappedBy = "request",        // el dueño de la relacion es CollectionItemEntity.request
            cascade = CascadeType.ALL,   // al guardar/borrar la solicitud, igual sus items
            orphanRemoval = true,        // si quito un item de la lista, se borra de la BD
            fetch = FetchType.LAZY       // no traigas los items hasta que alguien los pida
    )
    @Builder.Default                     // sin esto, el builder deja la lista en null
    private List<CollectionItemEntity> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------------
    // Callbacks de JPA: Hibernate los llama solo, antes de INSERT/UPDATE.
    // Asi nunca se te olvida poner las fechas en el service.
    // ---------------------------------------------------------------

    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        this.createdAt = ahora;
        this.updatedAt = ahora;
        if (this.status == null) {
            this.status = CollectionStatus.PENDING;
        }
    }

    @PreUpdate
    void alActualizar() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------------------------------------------------
    // Metodos de conveniencia para la relacion bidireccional.
    //
    // En un @OneToMany/@ManyToOne hay que actualizar LOS DOS lados o
    // Hibernate guarda el item con request_id = NULL. Es un error clasico
    // y muy confuso. Estos metodos lo hacen imposible.
    // ---------------------------------------------------------------

    public void addItem(CollectionItemEntity item) {
        this.items.add(item);
        item.setRequest(this);
    }

    public void removeItem(CollectionItemEntity item) {
        this.items.remove(item);
        item.setRequest(null);
    }
}

package com.collection_service.events;

import com.collection_service.entity.CollectionRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Publica los eventos del servicio en Kafka.
 *
 * DECISION IMPORTANTE: si Kafka esta caido, el envio falla pero la peticion
 * HTTP NO. Solo se registra el error en el log.
 *
 * El motivo: la recoleccion ya se guardo en MySQL, que es la fuente de la
 * verdad. Tumbar la respuesta del usuario porque un sistema secundario no
 * responde seria acoplar tu disponibilidad a la de Kafka, justo lo contrario
 * de lo que buscan los microservicios.
 *
 * (En produccion esto se resuelve con el patron outbox: guardar el evento en
 * una tabla dentro de la misma transaccion y reintentarlo despues. Para la
 * materia, registrar el fallo basta -- pero conviene saber que existe.)
 */
@Service
public class CollectionEventProducer {

    private static final Logger log = LoggerFactory.getLogger(CollectionEventProducer.class);

    public static final String TOPIC_REQUESTED = "collection-requested";
    public static final String TOPIC_ASSIGNED  = "collection-assigned";
    public static final String TOPIC_COMPLETED = "collection-completed";
    public static final String TOPIC_CANCELLED = "collection-cancelled";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CollectionEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarCreada(CollectionRequestEntity r) {
        enviar(TOPIC_REQUESTED, r.getId(), new CollectionRequestedEvent(
                r.getId(), r.getRequesterId(), r.getRecyclingCenterId(),
                r.getPickupLocation(), r.getScheduledAt(), r.getCreatedAt()));
    }

    public void publicarAsignada(CollectionRequestEntity r) {
        enviar(TOPIC_ASSIGNED, r.getId(), new CollectionAssignedEvent(
                r.getId(), r.getRequesterId(), r.getCollectorId(), r.getAssignedAt()));
    }

    public void publicarCompletada(CollectionRequestEntity r) {
        List<CollectionCompletedEvent.Item> items = r.getItems().stream()
                .filter(i -> i.getConfirmedWeightKg() != null)
                .map(i -> new CollectionCompletedEvent.Item(
                        i.getMaterialType(), i.getConfirmedWeightKg()))
                .toList();

        enviar(TOPIC_COMPLETED, r.getId(), new CollectionCompletedEvent(
                r.getId(), r.getRequesterId(), r.getCollectorId(),
                r.getRecyclingCenterId(), r.getCompletedAt(),
                r.getTotalWeightKg(), items));
    }

    public void publicarCerrada(CollectionRequestEntity r) {
        enviar(TOPIC_CANCELLED, r.getId(), new CollectionCancelledEvent(
                r.getId(), r.getRequesterId(), r.getStatus(),
                r.getCloseReason(), r.getClosedAt()));
    }

    /**
     * La clave del mensaje es el collectionId: Kafka garantiza el ORDEN por
     * clave, asi que los eventos de una misma recoleccion llegan en secuencia.
     */
    private void enviar(String topic, Long collectionId, Object evento) {
        try {
            kafkaTemplate.send(topic, String.valueOf(collectionId), evento)
                    .whenComplete((resultado, error) -> {
                        if (error != null) {
                            log.error("No se pudo publicar {} de la recoleccion {}: {}",
                                    topic, collectionId, error.getMessage());
                        } else {
                            log.info("Evento {} publicado para la recoleccion {}",
                                    topic, collectionId);
                        }
                    });
        } catch (Exception e) {
            log.error("Error al enviar a Kafka el topic {} ({}): {}",
                    topic, collectionId, e.getMessage());
        }
    }
}

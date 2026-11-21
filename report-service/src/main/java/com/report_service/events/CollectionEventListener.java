package com.report_service.events;

import com.report_service.service.ReportIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Los buzones de entrada del servicio.
 *
 * Fijate en containerFactory: cada listener usa la fabrica tipada para SU
 * evento. Sin eso, Jackson intentaria convertir todo al mismo tipo.
 *
 * El listener no tiene logica: delega al service. Igual que un controller
 * con HTTP, pero la "puerta" aqui es Kafka.
 */
@Component
public class CollectionEventListener {

    private static final Logger log = LoggerFactory.getLogger(CollectionEventListener.class);

    private final ReportIngestionService ingesta;

    public CollectionEventListener(ReportIngestionService ingesta) {
        this.ingesta = ingesta;
    }

    @KafkaListener(topics = "collection-completed",
                   containerFactory = "collectionCompletedFactory")
    public void alCompletarse(CollectionCompletedEvent evento) {
        log.info("Recibido collection-completed: recoleccion {}", evento.collectionId());
        ingesta.ingerirCompletada(evento);
    }

    @KafkaListener(topics = "collection-cancelled",
                   containerFactory = "collectionCancelledFactory")
    public void alCancelarse(CollectionCancelledEvent evento) {
        log.info("Recibido collection-cancelled: recoleccion {}", evento.collectionId());
        ingesta.ingerirCancelada(evento);
    }

    @KafkaListener(topics = "user-registered",
                   containerFactory = "userRegisteredFactory")
    public void alRegistrarseUsuario(UserRegisteredEvent evento) {
        log.info("Recibido user-registered: usuario {}", evento.userId());
        ingesta.ingerirUsuarioRegistrado(evento);
    }
}

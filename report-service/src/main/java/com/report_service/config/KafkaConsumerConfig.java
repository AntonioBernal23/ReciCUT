package com.report_service.config;

import com.report_service.events.CollectionCancelledEvent;
import com.report_service.events.CollectionCompletedEvent;
import com.report_service.events.UserRegisteredEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumidor de Kafka. UNA FABRICA POR TIPO DE EVENTO.
 *
 * Por que no se puede copiar la config de user-service:
 *
 *   config.put(VALUE_DEFAULT_TYPE, UserRegisteredEvent.class);
 *   config.put(USE_TYPE_INFO_HEADERS, false);
 *
 * Eso significa "TODO lo que llegue a este consumidor conviertelo a
 * UserRegisteredEvent". A tu companiero le sirve porque solo consume un tipo.
 *
 * Nosotros consumimos TRES. Con esa config, un CollectionCompletedEvent
 * intentaria convertirse a UserRegisteredEvent y tendrias un error de
 * deserializacion que parece inexplicable.
 *
 * La solucion: una fabrica por tipo, y cada @KafkaListener dice cual usar.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final String GRUPO = "report-service";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /** Arma una fabrica tipada para la clase que se le pase. */
    private <T> ConcurrentKafkaListenerContainerFactory<String, T> fabricaPara(Class<T> tipo) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, GRUPO);

        // earliest: al arrancar por primera vez, lee el topic DESDE EL PRINCIPIO.
        // Por eso vamos a poder procesar los eventos que collection-service ya
        // publico antes de que este servicio existiera.
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, tipo);
        config.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.report_service.events");

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(config));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CollectionCompletedEvent>
    collectionCompletedFactory() {
        return fabricaPara(CollectionCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CollectionCancelledEvent>
    collectionCancelledFactory() {
        return fabricaPara(CollectionCancelledEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent>
    userRegisteredFactory() {
        return fabricaPara(UserRegisteredEvent.class);
    }
}

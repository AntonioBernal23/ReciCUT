package com.collection_service.service;

import com.collection_service.dto.CompleteCollectionDTO;
import com.collection_service.dto.ConfirmItemWeightDTO;
import com.collection_service.entity.CollectionItemEntity;
import com.collection_service.entity.CollectionRequestEntity;
import com.collection_service.enums.CollectionStatus;
import com.collection_service.enums.MaterialType;
import com.collection_service.enums.MeasurementUnit;
import com.collection_service.events.CollectionEventProducer;
import com.collection_service.exception.InvalidStatusTransitionException;
import com.collection_service.repository.CollectionRequestRepository;
import com.collection_service.repository.CollectionStatusHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PRUEBAS 2 y 3 — El servicio de recolecciones.
 *
 * Con Mockito, sin Spring ni MySQL ni Kafka: el repositorio y el
 * productor son dobles. Lo que se prueba es la DECISION del servicio, no
 * la infraestructura.
 */
@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock CollectionRequestRepository requestRepository;
    @Mock CollectionStatusHistoryRepository historyRepository;
    @Mock CollectionEventProducer eventProducer;

    @InjectMocks CollectionService service;

    private static final Long ID = 7L;
    private static final Long RECOLECTOR = 3L;

    /** Una solicitud en curso, con un material pendiente de pesar. */
    private CollectionRequestEntity enCurso() {
        CollectionRequestEntity s = CollectionRequestEntity.builder()
                .id(ID)
                .requesterId(1L)
                .collectorId(RECOLECTOR)
                .recyclingCenterId(2L)
                .status(CollectionStatus.IN_PROGRESS)
                .pickupLocation("Edificio C, planta baja")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        s.addItem(CollectionItemEntity.builder()
                .id(100L)
                .materialType(MaterialType.PET)
                .estimatedQuantity(new BigDecimal("10"))
                .unit(MeasurementUnit.PIECE)
                .build());

        return s;
    }

    // =================================================================
    // PRUEBA 2: completar publica el evento
    // =================================================================

    /**
     * Si esta prueba falla, el sistema sigue "funcionando" de cara al
     * usuario —la recoleccion se marca completada— pero report-service
     * nunca se entera y los reportes se quedan vacios para siempre.
     *
     * Es el tipo de falla que no se ve hasta la demostracion.
     */
    @Test
    @DisplayName("Completar una recoleccion publica el evento para los reportes")
    void completarPublicaElEvento() {
        CollectionRequestEntity s = enCurso();
        when(requestRepository.findById(ID)).thenReturn(Optional.of(s));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.completar(ID, RECOLECTOR, "COLLECTOR",
                new CompleteCollectionDTO(
                        List.of(new ConfirmItemWeightDTO(100L, new BigDecimal("4.500"))),
                        null));

        verify(eventProducer).publicarCompletada(any(CollectionRequestEntity.class));
    }

    @Test
    @DisplayName("El peso total lo calcula el servidor, no el cliente")
    void elTotalLoCalculaElServidor() {
        CollectionRequestEntity s = enCurso();
        when(requestRepository.findById(ID)).thenReturn(Optional.of(s));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.completar(ID, RECOLECTOR, "COLLECTOR",
                new CompleteCollectionDTO(
                        List.of(new ConfirmItemWeightDTO(100L, new BigDecimal("4.500"))),
                        null));

        assertThat(s.getTotalWeightKg()).isEqualByComparingTo("4.500");
        assertThat(s.getStatus()).isEqualTo(CollectionStatus.COMPLETED);
    }

    // =================================================================
    // PRUEBA 3: transicion invalida
    // =================================================================

    /**
     * Completar algo que sigue en PENDING: nadie fue a recogerlo.
     *
     * Lo importante no es solo que falle, sino COMO falla:
     * InvalidStatusTransitionException la traduce el
     * GlobalExceptionHandler a 409 CONFLICT. Un 500 diria "se rompio el
     * servidor"; un 409 dice "tu peticion choca con el estado actual",
     * que es la verdad y es lo que el frontend sabe explicar.
     */
    @Test
    @DisplayName("Completar una solicitud PENDING es una transicion invalida (-> 409)")
    void completarPendienteEsInvalido() {
        CollectionRequestEntity s = enCurso();
        s.setStatus(CollectionStatus.PENDING);
        when(requestRepository.findById(ID)).thenReturn(Optional.of(s));

        assertThatThrownBy(() ->
                service.completar(ID, RECOLECTOR, "COLLECTOR",
                        new CompleteCollectionDTO(
                                List.of(new ConfirmItemWeightDTO(100L, new BigDecimal("4.5"))),
                                null)))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    /**
     * Y sobre todo: cuando la transicion es invalida NO se publica nada.
     *
     * Un evento es irreversible: una vez en Kafka, report-service ya lo
     * conto. Publicar primero y validar despues seria corromper los
     * reportes con datos que la propia solicitud rechazo.
     */
    @Test
    @DisplayName("Una transicion invalida no publica ningun evento")
    void transicionInvalidaNoPublicaNada() {
        CollectionRequestEntity s = enCurso();
        s.setStatus(CollectionStatus.COMPLETED);   // ya es final
        when(requestRepository.findById(ID)).thenReturn(Optional.of(s));

        assertThatThrownBy(() ->
                service.completar(ID, RECOLECTOR, "COLLECTOR",
                        new CompleteCollectionDTO(
                                List.of(new ConfirmItemWeightDTO(100L, new BigDecimal("4.5"))),
                                null)))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(eventProducer, never()).publicarCompletada(any());
        verify(requestRepository, never()).save(any());
    }
}

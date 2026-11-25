package com.collection_service.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PRUEBA 1 — La maquina de estados.
 *
 * Es la regla de negocio mas importante del servicio: dice que puede
 * pasarle a una recoleccion y que no. Si se rompe, el sistema deja
 * completar cosas que nunca se recogieron, y los reportes mienten.
 *
 * No necesita Spring ni base de datos: el enum es logica pura. Corre en
 * milisegundos, que es justo lo que quieres de la prueba que mas veces
 * vas a ejecutar.
 */
class CollectionStatusTest {

    @Nested
    @DisplayName("El camino feliz")
    class CaminoFeliz {

        @Test
        @DisplayName("PENDING -> ASSIGNED -> IN_PROGRESS -> COMPLETED es valido paso a paso")
        void flujoCompleto() {
            assertThat(CollectionStatus.PENDING.puedeCambiarA(CollectionStatus.ASSIGNED)).isTrue();
            assertThat(CollectionStatus.ASSIGNED.puedeCambiarA(CollectionStatus.IN_PROGRESS)).isTrue();
            assertThat(CollectionStatus.IN_PROGRESS.puedeCambiarA(CollectionStatus.COMPLETED)).isTrue();
        }
    }

    @Nested
    @DisplayName("Los atajos prohibidos")
    class AtajosProhibidos {

        /**
         * El caso que motiva toda la prueba: sin esta regla alguien podria
         * marcar como recogida una solicitud que nadie fue a recoger, y
         * report-service lo contaria como material reciclado real.
         */
        @Test
        @DisplayName("PENDING no puede saltar directo a COMPLETED")
        void noSePuedeSaltarAlFinal() {
            assertThat(CollectionStatus.PENDING.puedeCambiarA(CollectionStatus.COMPLETED)).isFalse();
        }

        @Test
        @DisplayName("PENDING tampoco puede saltarse la asignacion")
        void noSePuedeEmpezarSinAsignar() {
            assertThat(CollectionStatus.PENDING.puedeCambiarA(CollectionStatus.IN_PROGRESS)).isFalse();
        }

        @Test
        @DisplayName("Una recoleccion no puede retroceder")
        void noSePuedeRetroceder() {
            assertThat(CollectionStatus.IN_PROGRESS.puedeCambiarA(CollectionStatus.PENDING)).isFalse();
            assertThat(CollectionStatus.ASSIGNED.puedeCambiarA(CollectionStatus.PENDING)).isFalse();
        }
    }

    @Nested
    @DisplayName("Los estados finales")
    class EstadosFinales {

        @Test
        @DisplayName("COMPLETED, CANCELLED y REJECTED son finales")
        void sonFinales() {
            assertThat(CollectionStatus.COMPLETED.esFinal()).isTrue();
            assertThat(CollectionStatus.CANCELLED.esFinal()).isTrue();
            assertThat(CollectionStatus.REJECTED.esFinal()).isTrue();
        }

        @Test
        @DisplayName("Los estados en curso NO son finales")
        void noSonFinales() {
            assertThat(CollectionStatus.PENDING.esFinal()).isFalse();
            assertThat(CollectionStatus.ASSIGNED.esFinal()).isFalse();
            assertThat(CollectionStatus.IN_PROGRESS.esFinal()).isFalse();
        }

        /**
         * De un estado final no sale NADA. Importa porque la interfaz dibuja
         * sus botones a partir de siguientesPosibles(): si aqui saliera algo,
         * la pantalla ofreceria acciones imposibles.
         */
        @Test
        @DisplayName("De un estado final no sale ninguna transicion")
        void deUnFinalNoSaleNada() {
            assertThat(CollectionStatus.COMPLETED.siguientesPosibles()).isEmpty();
            assertThat(CollectionStatus.CANCELLED.siguientesPosibles()).isEmpty();
            assertThat(CollectionStatus.REJECTED.siguientesPosibles()).isEmpty();
        }
    }

    @Test
    @DisplayName("Ningun estado puede cambiar a si mismo")
    void nadieCambiaASiMismo() {
        for (CollectionStatus estado : CollectionStatus.values()) {
            assertThat(estado.puedeCambiarA(estado))
                    .as("%s no deberia poder cambiar a si mismo", estado)
                    .isFalse();
        }
    }
}

package com.recicut.recolectores;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba generada por Spring Initializr: levanta el contexto completo.
 *
 * DESACTIVADA a proposito. No es una prueba unitaria: para arrancar
 * necesita el config-server, Eureka y la base de datos de este servicio.
 * Sin la pila levantada falla siempre, y hacia que `./mvnw test` fallara
 * en TODOS los modulos, tapando las pruebas que si prueban algo.
 *
 * Que el contexto arranca ya se comprueba cada vez que se levanta el
 * sistema con Docker; si un bean esta mal cableado, el contenedor no
 * pasa de "starting".
 *
 * Para correrla a mano, con los contenedores arriba:
 *     ./mvnw test -Dtest=*ApplicationTests -DexcludedGroups=
 */
@Disabled("Necesita config-server, Eureka y la base de datos levantados")
@SpringBootTest
class RecolectoresApplicationTests {

	@Test
	void contextLoads() {
	}

}

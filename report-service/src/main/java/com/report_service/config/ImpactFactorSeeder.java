package com.report_service.config;

import com.report_service.entity.MaterialImpactFactorEntity;
import com.report_service.enums.MaterialType;
import com.report_service.repository.MaterialImpactFactorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Carga el catalogo de factores de impacto al arrancar, si esta vacio.
 *
 * CommandLineRunner: Spring ejecuta su metodo run() una vez que la aplicacion
 * termino de arrancar. Es el lugar estandar para datos semilla.
 *
 * SOBRE LOS NUMEROS: son estimaciones de orden de magnitud, dentro de los
 * rangos que se manejan habitualmente para estos materiales (el tipo de
 * cifras que publican la EPA en su modelo WARM y la SEMARNAT). NO estan
 * tomadas una por una de una fuente primaria ni auditadas.
 *
 * La decision fue consciente: para este proyecto lo que se evalua es la
 * arquitectura, no la exactitud del dato ambiental. Por eso cada registro
 * lo dice en su sourceReference y la pantalla de reportes lo advierte,
 * en lugar de presentar como exacto un numero que no lo es.
 *
 * Cada factor responde a "cuanto CO2 se EVITA al reciclar un kilo en vez
 * de mandarlo al relleno sanitario", que no es lo mismo que "cuanto CO2
 * cuesta producir ese kilo". Son dos cifras distintas y se confunden
 * seguido; por eso el aluminio sale tan alto: fundir chatarra consume una
 * fraccion de la energia de producirlo desde la bauxita.
 *
 * Si mas adelante se quieren numeros defendibles, se cambian aqui y en
 * ningun otro lado: el calculo los lee de la base de datos.
 */
@Component
public class ImpactFactorSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImpactFactorSeeder.class);
    /**
     * Lo que queda guardado en cada factor. El texto es deliberadamente
     * explicito: cualquiera que consulte la tabla sabe que tan lejos
     * puede llevar el numero.
     */
    private static final String FUENTE =
            "Estimacion de orden de magnitud, dentro de rangos de referencia "
            + "habituales (tipo EPA WARM / SEMARNAT). No verificada contra "
            + "fuente primaria. Mide CO2 evitado frente a relleno sanitario.";

    private final MaterialImpactFactorRepository repo;

    public ImpactFactorSeeder(MaterialImpactFactorRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;   // ya estaba cargado, no hacer nada
        }

        sembrar(MaterialType.PET,         "2.0000", "60.00",  "1.8000", "Plastico PET");
        sembrar(MaterialType.HDPE,        "1.8000", "50.00",  "1.6000", "Plastico HDPE");
        sembrar(MaterialType.PAPER,       "0.9000", "26.00",  "0.9000", "Papel");
        sembrar(MaterialType.CARDBOARD,   "0.8000", "24.00",  "0.8000", "Carton");
        sembrar(MaterialType.ALUMINUM,    "9.0000", "40.00",  "14.0000", "Aluminio");
        sembrar(MaterialType.GLASS,       "0.3000", "8.00",   "0.3000", "Vidrio");
        sembrar(MaterialType.TETRA_PAK,   "0.8000", "20.00",  "0.7000", "Envase multicapa");
        sembrar(MaterialType.ELECTRONIC,  "1.4000", "15.00",  "1.2000", "Residuo electronico");
        sembrar(MaterialType.BATTERY,     "1.0000", "10.00",  "0.5000", "Pilas");
        sembrar(MaterialType.BOTTLE_CAP,  "2.0000", "60.00",  "1.8000", "Tapas de plastico");
        sembrar(MaterialType.ORGANIC,     "0.2500", "2.00",   "0.1000", "Organico para composta");
        sembrar(MaterialType.OTHER,       "0.0000", "0.00",   "0.0000", "Sin factor asignado");

        log.info("Catalogo de impacto ambiental cargado: {} materiales", repo.count());
    }

    private void sembrar(MaterialType tipo, String co2, String agua,
                         String energia, String descripcion) {
        repo.save(MaterialImpactFactorEntity.builder()
                .materialType(tipo)
                .co2AvoidedKgPerKg(new BigDecimal(co2))
                .waterSavedLitersPerKg(new BigDecimal(agua))
                .energySavedKwhPerKg(new BigDecimal(energia))
                .description(descripcion)
                .sourceReference(FUENTE)
                .build());
    }
}

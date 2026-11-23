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
 * ATENCION PARA LA EXPOSICION: estos valores son aproximaciones de referencia
 * de uso comun, NO estan tomados de una fuente verificada. Antes de entregar,
 * busquen numeros de SEMARNAT, la EPA o un articulo academico y llenen
 * sourceReference. Si el profesor pregunta de donde salieron y la respuesta
 * es "los pusimos nosotros", resta. Si es "los tomamos de X", suma.
 */
@Component
public class ImpactFactorSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImpactFactorSeeder.class);
    private static final String PENDIENTE = "PENDIENTE: citar fuente verificada";

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
                .sourceReference(PENDIENTE)
                .build());
    }
}

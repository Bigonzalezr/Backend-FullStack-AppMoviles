package com.appmovil.msvc.boleta.init;

import com.appmovil.msvc.boleta.models.entities.Boleta;
import com.appmovil.msvc.boleta.repositories.BoletaRepository;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

@Profile("dev")
@Component
public class LoadDatabase implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Autowired
    private BoletaRepository boletaRepository;

    @Override
    public void run(String... args) throws Exception {
        Faker faker = new Faker(new Locale("es", "CL"));

        if (boletaRepository.count() == 0) {
            for (int i = 0; i < 100; i++) {
                Boleta boleta = new Boleta();
                boleta.setHoraBoleta(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30)));
                boleta.setTotal(faker.number().numberBetween(10000, 50000));
                boleta.setIdCurso((long) faker.number().numberBetween(100, 200));
                boleta.setIdProfesor((long) faker.number().numberBetween(200, 300));
                boleta.setIdAlumno((long) faker.number().numberBetween(300, 400));

                boleta = boletaRepository.save(boleta);
                log.info("La boleta creada es {}", boleta);
            }
        }
    }
}

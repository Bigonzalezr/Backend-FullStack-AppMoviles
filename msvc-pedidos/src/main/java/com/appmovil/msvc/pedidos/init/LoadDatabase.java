package com.appmovil.msvc.pedidos.init;

import com.appmovil.msvc.pedidos.model.entity.Pedido;
import com.appmovil.msvc.pedidos.model.entity.PedidoDetalle;
import com.appmovil.msvc.pedidos.repositories.PedidoRepository;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Profile("dev")
@Component
public class LoadDatabase implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Autowired
    private PedidoRepository pedidoRepository; // Corregido de CompraRepository

    @Override
    public void run(String... args) throws Exception {
        Faker faker = new Faker(new Locale("es", "CL"));

        if (pedidoRepository.count() == 0) {
            log.info("Iniciando la precarga de 100 Pedidos de LevelUp Gamer...");

            for (int i = 0; i < 100; i++) {
                // 1. Crear la cabecera del Pedido
                Pedido pedido = new Pedido();

                // IDs de prueba para Usuario/Producto (deben coincidir con otros MSVC)
                pedido.setIdUsuario((long) faker.number().numberBetween(1, 10)); // 10 usuarios de prueba

                // 2. Definir la fecha
                pedido.setFechaCompra(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 90)));

                // 3. Crear los detalles (productos)
                int numDetalles = faker.number().numberBetween(1, 4);

                for (int j = 0; j < numDetalles; j++) {
                    PedidoDetalle detalle = new PedidoDetalle();
                    detalle.setIdProducto((long) faker.number().numberBetween(1, 15)); // 15 productos de prueba
                    detalle.setNombreProducto(faker.commerce().productName());
                    detalle.setPrecioUnitario(faker.number().numberBetween(10000, 150000));
                    detalle.setCantidad(faker.number().numberBetween(1, 3));

                    pedido.agregarDetalle(detalle);
                }

                // El método @PrePersist/@PreUpdate de Pedido.java calculará los totales y el estado.
                pedido.setEstado(faker.options().option("COMPLETADO", "RECHAZADO", "PENDIENTE", "ENVIADO"));

                // 4. Guardar la entidad
                pedido = pedidoRepository.save(pedido);
                log.info("Pedido creado: ID {} para Usuario {}", pedido.getId(), pedido.getIdUsuario());
            }
            log.info("Precarga de Pedidos finalizada. Total: {}", pedidoRepository.count());
        }
    }
}
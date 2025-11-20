package com.appmovil.msvc.pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("APIRESTFULL - MSVC - Pedido (LevelUp Gamer)")
                        .description("Esta es la sección donde se encuentran todos " +
                                "los endpoints para la gestión de Pedidos/Órdenes de Compra.")
                        .version("1.0.0")
                );
    }
}
package com.appmovil.msvc.carrito.config;

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
                        .title("APIRESTFULL - MSVC - Carrito")
                        .description("Esta es la sección donde se encuentran todos " +
                                "los endpoints del microservicio de carrito de compras")
                        .version("1.0.0")
                );
    }
}

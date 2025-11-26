package com.appmovil.msvc.productos.config;

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
                        .title("APIRESTFULL - MSVC - Productos")
                        .description("Esta es la sección donde se encuentran todos " +
                                "los endpoints del microservicio de productos")
                        .version("1.0.0")
                );
    }
}

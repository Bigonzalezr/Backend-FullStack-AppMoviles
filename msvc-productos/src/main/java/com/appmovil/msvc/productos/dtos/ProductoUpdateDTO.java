package com.appmovil.msvc.productos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductoUpdateDTO {
    // Excluimos el ID (se toma del PathVariable en el Controller)

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    private Integer precio;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    private String imagen;
    private String descripcion;

    @NotNull(message = "El stock es obligatorio")
    private Integer stock;


    private Double rating;
}
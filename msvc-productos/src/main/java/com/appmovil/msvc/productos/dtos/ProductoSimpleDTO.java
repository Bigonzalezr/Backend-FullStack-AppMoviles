package com.appmovil.msvc.productos.dtos;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProductoSimpleDTO {

    private Long id;
    private String nombre;
    private String imagen;
    private Integer precio;
}
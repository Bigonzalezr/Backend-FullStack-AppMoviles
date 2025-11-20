package com.appmovil.msvc.productos.dtos;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProductoLoadDTO {

    private String nombre;
    private Integer precio;
    private String categoria;
    private String imagen;
    private String descripcion;
    private Integer stock;
}
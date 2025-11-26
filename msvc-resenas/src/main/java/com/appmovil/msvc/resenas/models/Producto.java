package com.appmovil.msvc.resenas.models;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Long id;
    private String nombre;
    private Double precio;
    private String categoria;
    private String imagen;
    private String descripcion;
    private Integer stock;
    private Boolean activo;
    private Double rating;
}

package com.appmovil.msvc.admin.models;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Long idProducto;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String categoria;
    private String imagen;
    private Boolean activo;
    private Double rating;
}

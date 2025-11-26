package com.appmovil.msvc.pedidos.model;

import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {
    private Long idProducto;
    private String nombre;
    private String descripcion;
    private Integer precio;
    private Integer stock;
    private String categoria;
    private String imagen;
    private Boolean activo;
    private Double rating;
}
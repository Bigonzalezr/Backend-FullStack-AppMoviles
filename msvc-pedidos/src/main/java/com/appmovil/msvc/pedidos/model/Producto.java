package com.appmovil.msvc.pedidos.model;

import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    private Long id;
    private String nombre;
    private String categoria;
    private Integer precio;
    private String imagen;
    private Integer stock;

}
package com.appmovil.msvc.productos.models;

import lombok.*;

// Clase que representa un producto en el contexto de un carrito/pedido.
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem {

    private Long idProducto;
    private String nombre;
    private String imagen;
    private Integer precioUnitario;

}
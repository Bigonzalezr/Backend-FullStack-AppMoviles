package com.appmovil.msvc.pedidos.dtos;

import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class ProductoDTO {
    private Long idProducto;
    private String nombre;
    private String imagen;
}
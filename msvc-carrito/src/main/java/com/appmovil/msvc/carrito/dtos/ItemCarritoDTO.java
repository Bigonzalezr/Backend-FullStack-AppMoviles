package com.appmovil.msvc.carrito.dtos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarritoDTO {
    private Long idItem;
    private Long idProducto;
    private String nombreProducto;
    private String imagenProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}

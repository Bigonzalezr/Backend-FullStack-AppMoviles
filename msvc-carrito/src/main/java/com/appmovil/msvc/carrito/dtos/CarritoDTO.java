package com.appmovil.msvc.carrito.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDTO {
    private Long idCarrito;
    private Long idUsuario;
    private String nombreUsuario;
    private List<ItemCarritoDTO> items;
    private BigDecimal total;
    private String estado;
}

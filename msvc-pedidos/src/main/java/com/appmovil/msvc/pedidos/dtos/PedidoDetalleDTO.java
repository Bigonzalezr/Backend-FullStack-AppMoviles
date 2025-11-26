package com.appmovil.msvc.pedidos.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDetalleDTO {

    private Long idDetalle;
    
    @NotNull(message = "El ID de producto es obligatorio")
    private Long idProducto;

    private String nombreProducto;
    
    private Integer precioUnitario;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad;
    
    private Integer subtotal;
}
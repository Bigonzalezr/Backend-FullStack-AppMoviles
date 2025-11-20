package com.appmovil.msvc.pedidos.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class PedidoCreationDTO {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long idUsuario;

    @NotNull(message = "La lista de ítems no puede ser nula")
    @Size(min = 1, message = "El pedido debe contener al menos un ítem")
    @Valid
    private List<PedidoDetalleDTO> detalles;

    private String metodoPago;
}
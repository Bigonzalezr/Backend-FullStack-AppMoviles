package com.appmovil.msvc.pedidos.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoCreationDTO {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long idUsuario;

    @NotNull(message = "La lista de ítems no puede ser nula")
    @Size(min = 1, message = "El pedido debe contener al menos un ítem")
    @Valid
    private List<PedidoDetalleDTO> detalles;

    @NotNull(message = "La dirección de envío es obligatoria")
    @Size(min = 10, max = 500, message = "La dirección debe tener entre 10 y 500 caracteres")
    private String direccionEnvio;

    private String metodoPago;
    
    private String notas;
}
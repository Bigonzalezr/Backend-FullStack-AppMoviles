package com.appmovil.msvc.admin.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    private Long idPedido;
    private Long idUsuario;
    private LocalDateTime fechaPedido;
    private String estado; // PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO
    private Double total;
    private String direccionEnvio;
    private String metodoPago;
}

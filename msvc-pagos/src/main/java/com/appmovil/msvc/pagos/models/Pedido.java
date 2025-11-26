package com.appmovil.msvc.pagos.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {
    private Long idPedido;
    private Long idUsuario;
    private LocalDateTime fechaPedido;
    private String estado;
    private BigDecimal total;
    private String direccionEnvio;
    private String metodoPago;
}

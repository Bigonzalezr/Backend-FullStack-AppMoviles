package com.appmovil.msvc.pedidos.model;

import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    private Long idPago;
    private Long idPedido;
    private Long idUsuario;

    private Integer montoTotal;
    private String metodoPago;

    private String estadoTransaccion;
    private LocalDateTime fechaPago;
}
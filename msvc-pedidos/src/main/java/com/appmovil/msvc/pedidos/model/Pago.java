package com.appmovil.msvc.pedidos.model;

import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
    private Long idPago;
    private Long idPedido;
    private Long idUsuario;
    private Integer monto;
    private String metodoPago;
    private String estado;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaProcesamiento;
    private String numeroTransaccion;
    private String numeroAutorizacion;
    private String gatewayPago;
    private String descripcion;
    private String mensajeError;
}
package com.appmovil.msvc.pagos.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {
    private Long idPago;
    private Long idPedido;
    private Long idUsuario;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaProcesamiento;
    private String numeroTransaccion;
    private String numeroAutorizacion;
    private String gatewayPago;
    private String ultimosDigitosTarjeta;
    private String descripcion;
    private String mensajeError;
    
    // Datos enriquecidos
    private String nombreUsuario;
    private String emailUsuario;
    private BigDecimal totalPedido;
    private String estadoPedido;
}

package com.appmovil.msvc.pagos.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesarPagoDTO {
    
    @NotNull(message = "El ID del pedido es obligatorio")
    private Long idPedido;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long idUsuario;
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;
    
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "TARJETA_CREDITO|TARJETA_DEBITO|PAYPAL|TRANSFERENCIA", 
             message = "Método de pago inválido")
    private String metodoPago;
    
    private String gatewayPago; // Stripe, PayPal, MercadoPago
    
    private String ultimosDigitosTarjeta;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
}
